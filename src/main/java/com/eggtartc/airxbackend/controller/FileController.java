package com.eggtartc.airxbackend.controller;

import com.eggtartc.airxbackend.controller.generic.BaseController;
import com.eggtartc.airxbackend.entity.File;
import com.eggtartc.airxbackend.entity.FileShare;
import com.eggtartc.airxbackend.entity.FileStore;
import com.eggtartc.airxbackend.entity.User;
import com.eggtartc.airxbackend.util.FileUtils;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@CrossOrigin("*")
public class FileController extends BaseController {
    @Value("${airx.file_save_path}")
    private String fileSavePath;

    @GetMapping("/api/v1/file")
    public ResponseEntity<Page<File>> myFiles(@AuthenticationPrincipal Jwt jwt, Pageable pageable, @RequestParam String search) {
        Optional<User> userOpt = getUserFromJwtToken(jwt);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userOpt.get();
        return ResponseEntity.ok(
            fileRepository.findAllByUser_IdAndNameContains(pageable, user.getId(), search));
    }

    @PostMapping("/api/v1/try-upload")
    public FileTryUploadResponse tryUpload(@AuthenticationPrincipal Jwt jwt, @RequestBody FileTryUploadRequest request) {
        Optional<User> userOpt = getUserFromJwtToken(jwt);
        if (userOpt.isEmpty()) {
            return FileTryUploadResponse.fail("Invalid user");
        }
        User user = userOpt.get();

        Optional<FileStore> storeOpt = fileStoreRepository.findBySha256(request.getSha256());
        if (storeOpt.isEmpty()) {
            return FileTryUploadResponse.fail("No file with this hash");
        }

        if (createFileFor(user, storeOpt.get()).isEmpty()) {
            return FileTryUploadResponse.fail("You already have this file!");
        }

        return FileTryUploadResponse.success();
    }

    @GetMapping("/api/v1/file/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Integer id) {
        Optional<File> fileOpt = fileRepository.findById(id);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        File file = fileOpt.get();
        FileStore store;
        java.io.File fileOnDisk;
        InputStreamResource fileResource;

        try {
            store = file.getFileStore();
            String absolutePath = store.getAbsolutePath();
            fileOnDisk = new java.io.File(absolutePath);
            fileResource = new InputStreamResource(new FileInputStream(fileOnDisk));
        }
        catch (Exception e) {
            Logger.getLogger("FileController").warning(e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        String encodedFileName = URLEncoder.encode(store.getFileName(), StandardCharsets.UTF_8)
            .replace("+", "%20");

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment;filename*=UTF-8''" + encodedFileName)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(store.getSize())
            .body(fileResource);
    }

    @DeleteMapping("/api/v1/file/{id}")
    public FileResponse delete(@PathVariable Integer id) {
        Optional<File> fileOpt = fileRepository.findById(id);
        if (fileOpt.isEmpty()) {
            return FileDeleteResponse.fail("No such file");
        }
        File file = fileOpt.get();
        FileStore store;
        java.io.File fileOnDisk;

        try {
            store = file.getFileStore();
            String absolutePath = store.getAbsolutePath();
            fileOnDisk = new java.io.File(absolutePath);
        }
        catch (Exception e) {
            Logger.getLogger("FileController").warning(e.getMessage());
            return FileDeleteResponse.fail("Failed to get file on disk");
        }

        try {
            fileRepository.delete(file);
            if (store.getFiles().size() == 0) {
                fileStoreRepository.delete(store);
                if (!fileOnDisk.delete()) {
                    Logger.getLogger("FileController").warning("Failed to delete file on disk");
                }
            }
        }
        catch (Exception e) {
            Logger.getLogger("FileController").warning(e.getMessage());
            return FileDeleteResponse.fail("Please stop sharing this file to delete it.");
        }

        return FileDeleteResponse.success("File deleted");
    }

    @PutMapping("/api/v1/file/{id}")
    public FileResponse update(@PathVariable Integer id, @RequestBody FileUpdateRequest request) {
        Optional<File> fileOpt = fileRepository.findById(id);
        if (fileOpt.isEmpty()) {
            return FileUpdateResponse.fail("No such file");
        }
        File file = fileOpt.get();
        file.setName(request.getName());

        try {
            fileRepository.save(file);
        }
        catch (Exception e) {
            Logger.getLogger("FileController").warning(e.getMessage());
            return FileUpdateResponse.fail("Failed to update file");
        }

        return FileUpdateResponse.success("File updated");
    }

    @PostMapping(value = "/api/v1/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileResponse upload(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file) {
        Optional<User> userOpt = getUserFromJwtToken(jwt);
        if (userOpt.isEmpty()) {
            return FileUploadResponse.fail("Invalid user");
        }
        User user = userOpt.get();

        Path savingAbsolutePath = Paths.get(fileSavePath)
            .toAbsolutePath().normalize();
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            return FileUploadResponse.fail("File name is null");
        }

        if (!savingAbsolutePath.toFile().exists()) {
            try {
                Files.createDirectories(savingAbsolutePath);
            }
            catch (Exception e) {
                // Exist ok.
            }
        }

        savingAbsolutePath = savingAbsolutePath.resolve(fileName);

        // Write to the file
        try {
            file.transferTo(savingAbsolutePath);
        }
        catch (Exception e) {
            return FileUploadResponse.fail("File transfer failed: " + e);
        }

        // Create file store and file
        String sha256;
        try {
            sha256 = FileUtils.sha256(file.getInputStream());
            FileStore store = FileStore.builder()
                .fileName(fileName)
                .size(file.getSize())
                .absolutePath(savingAbsolutePath.toString())
                .sha256(sha256)
                .uploadedAt(Timestamp.from(Instant.now()))
                .build();
            fileStoreRepository.save(store);
            createFileFor(user, store);
        }
        catch (Exception e) {
            return FileUploadResponse.fail("File store failed: " + e);
        }

        return FileUploadResponse.success("Success");
    }

    @GetMapping("/api/v1/share/{alias}")
    public QueryShareResponse queryShare(@PathVariable String alias) {
        Optional<FileShare> shareOpt = fileShareRepository.findByAlias(alias);
        if (shareOpt.isEmpty()) {
            return QueryShareResponse.fail("No such share");
        }
        FileShare share = shareOpt.get();

        return QueryShareResponse.success(share.getFile());
    }

    @GetMapping("/api/v1/share")
    public ResponseEntity<Page<FileShare>> myFiles(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        Optional<User> userOpt = getUserFromJwtToken(jwt);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userOpt.get();

        return ResponseEntity.ok(fileShareRepository.findAllByUser_Id(pageable, user.getId()));
    }

    @PostMapping("/api/v1/share")
    public FileShareResponse createShare(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody FileShareRequest request
    ) {
        Optional<User> userOpt = getUserFromJwtToken(jwt);
        if (userOpt.isEmpty()) {
            return FileShareResponse.fail("Invalid user");
        }
        User user = userOpt.get();

        Optional<File> fileOpt = fileRepository.findById(request.getFileId());
        if (fileOpt.isEmpty()) {
            return FileShareResponse.fail("No such file");
        }
        File file = fileOpt.get();

        // Make alias
        long now = Instant.now().toEpochMilli();
        long aliasValue = (now + user.getUid()) * (long) (Math.random() * 1000);
        String alias = Long.toUnsignedString(aliasValue, 36);

        // Create share
        FileShare share = FileShare.builder()
            .visits(0)
            .downloads(0)
            .file(file)
            .user(user)
            .alias(alias)
            .build();

        try {
            fileShareRepository.save(share);
            return FileShareResponse.success(alias);
        }
        catch (Exception e) {
            Logger.getLogger("FileController").warning(e.getMessage());
            return FileShareResponse.fail("Already shared!");
        }
    }

    @DeleteMapping("/api/v1/share/{alias}")
    public FileShareResponse deleteShare(@PathVariable String alias) {
        Optional<FileShare> shareOpt = fileShareRepository.findByAlias(alias);
        if (shareOpt.isEmpty()) {
            return FileShareResponse.fail("No such share");
        }
        FileShare share = shareOpt.get();

        try {
            fileShareRepository.delete(share);
        }
        catch (Exception e) {
            Logger.getLogger("FileController").warning(e.getMessage());
            return FileShareResponse.fail("Failed to delete share");
        }

        return FileShareResponse.success("Share deleted");
    }

    private Optional<File> createFileFor(User user, FileStore withStore) {
        File file = File.builder()
            .user(user)
            .fileStore(withStore)
            .name(withStore.getFileName())
            .build();
        try {
            return Optional.of(fileRepository.save(file));
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    @Data
    static class QueryShareRequest {
        String alias;
    }

    @Data
    static class FileShareRequest {
        Integer fileId;
    }

    @Data
    static class FileTryUploadRequest {
        String sha256;
    }

    @Data
    static class FileUpdateRequest {
        String name;
    }

    @Data
    @Builder
    static class QueryShareResponse {
        Boolean success;
        String message;
        File file;

        public static QueryShareResponse success(File file) {
            return new QueryShareResponse(true, "Success", file);
        }

        public static QueryShareResponse fail(String message) {
            return new QueryShareResponse(false, message, null);
        }
    }

    @Data
    @Builder
    static class FileTryUploadResponse {
        Boolean success;
        String message;

        public static FileTryUploadResponse success() {
            return new FileTryUploadResponse(true, "File matched!");
        }

        public static FileTryUploadResponse fail(String message) {
            return new FileTryUploadResponse(false, message);
        }
    }

    @Data
    @Builder
    static class FileShareResponse {
        Boolean success;
        String message;
        String shareId;

        public static FileShareResponse success(String shareId) {
            return new FileShareResponse(true, "File shared!", shareId);
        }

        public static FileShareResponse fail(String message) {
            return new FileShareResponse(false, message, null);
        }
    }

    @Data
    @Builder
    static class FileResponse {
        Boolean success;
        String message;

        public static FileResponse success(String message) {
            return new FileResponse(true, message);
        }

        public static FileResponse fail(String message) {
            return new FileResponse(false, message);
        }
    }

    static class FileDeleteResponse extends FileResponse {
        FileDeleteResponse(Boolean success, String message) {
            super(success, message);
        }
    }

    static class FileUpdateResponse extends FileResponse {

        FileUpdateResponse(Boolean success, String message) {
            super(success, message);
        }
    }

    static class FileUploadResponse extends FileResponse {
        FileUploadResponse(Boolean success, String message) {
            super(success, message);
        }
    }
}
