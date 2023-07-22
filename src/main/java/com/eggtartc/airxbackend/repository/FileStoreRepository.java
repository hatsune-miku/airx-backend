package com.eggtartc.airxbackend.repository;

import com.eggtartc.airxbackend.entity.FileStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileStoreRepository extends JpaRepository<FileStore, Integer> {
    Optional<FileStore> findBySha256(String sha256);

    boolean existsBySha256(String sha256);
}
