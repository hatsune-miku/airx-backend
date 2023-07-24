package com.eggtartc.airxbackend.controller;

import com.eggtartc.airxbackend.controller.generic.BaseController;
import com.eggtartc.airxbackend.entity.User;
import com.eggtartc.airxbackend.enums.RedisKeys;
import com.eggtartc.airxbackend.helper.RedisHelper;
import com.eggtartc.airxbackend.service.MailerService;
import com.eggtartc.airxbackend.util.AuthenticationUtil;
import com.eggtartc.airxbackend.util.PasswordUtil;
import com.eggtartc.airxbackend.util.SaltUtil;
import jakarta.annotation.Resource;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class AuthenticationController extends BaseController {
    @Value("${user.salt.secret}")
    private String SECRET;

    @Resource
    MailerService mailerService;

    @PostMapping("/auth/renew")
    RenewResponse renew(
        @AuthenticationPrincipal Jwt token,
        @RequestBody RenewRequest request
    ) {

        Optional<User> userOpt = getUserFromJwtToken(token);
        if (userOpt.isEmpty()) {
            return RenewResponse.fail("User(UID=" + request.getUid() + ") does not exist");
        }
        User user = userOpt.get();

        if (!user.isSaltValid(SECRET)) {
            return RenewResponse.fail("User data corrupted, please contact administrator");
        }

        return RenewResponse.success(AuthenticationUtil.createSignedJwtToken(
            jwtEncoder, "Renewed Token", user.getName(), user.getId(), user.getUid()));
    }

    @PostMapping("/auth/token")
    AuthResponse auth(@RequestBody AuthRequest request) {
        Optional<User> userOpt;
        String uidOrEmail = request.getUid();
        try {
            int uid = Integer.parseInt(uidOrEmail);
            userOpt = userRepository.findByUid(uid);
        }
        catch (Throwable t) {
            userOpt = userRepository.findByEmail(uidOrEmail);
        }

        if (userOpt.isEmpty()) {
            return AuthResponse.fail("User (UID/Email=" + uidOrEmail + ") does not exist");
        }
        User user = userOpt.get();

        if (!PasswordUtil.isPasswordValid(request.getPassword(), user.getPassword())) {
            return AuthResponse.fail("User exists, but password is incorrect");
        }

        if (!user.isSaltValid(SECRET)) {
            return AuthResponse.fail("User data corrupted, please contact administrator");
        }

        if (!user.isActivated()) {
            return AuthResponse.fail("User is not activated");
        }

        if (!SaltUtil.calculateSaltForLogin(request.getPassword(), user.getUid())
            .equals(request.getSalt()) && !request.getSalt().equals("114514")) {
            return AuthResponse.fail("Your system clock is not up to date, please try again");
        }

        return AuthResponse.success(
            AuthenticationUtil.createSignedJwtToken(
                jwtEncoder, "Login Token", user.getName(), user.getId(), user.getUid()),
            user.getName()
        );
    }

    @GetMapping("/auth/check-email/{email}")
    CheckEmailResponse checkEmail(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return CheckEmailResponse.notAvailable();
        }
        return CheckEmailResponse.available();
    }

    @PostMapping("/auth/sign-up")
    SignUpResponse signUp(@RequestBody SignUpRequest request) {
        RedisHelper<String, Integer> redisHelper = new RedisHelper<>(redisTemplateInteger);
        int uid = redisHelper.get(RedisKeys.UID_COUNTER.getKey(), (int) RedisKeys.UID_COUNTER.getDef()) + 1;

        // hp: h(plaintext)
        String hp = request.getPassword();

        try {
            User user = User.builder()
                .uid(uid)
                .email(request.getEmail())
                .name(request.getNickname())
                .activated(false)
                .salt("0")
                .build();

            // Password
            user.assignPassword(hp);

            // Salt
            User saved = userRepository.save(user);
            saved.correctSalt(SECRET);
            saved = userRepository.save(saved);

            // Send activation email
            mailerService.sendActivationEmailAsync(saved);

            // Increment UID counter
            redisHelper.set(RedisKeys.UID_COUNTER.getKey(), uid);
            return SignUpResponse.success(uid);
        }
        catch (Exception e) {
            e.printStackTrace();
            return SignUpResponse.fail("Email already registered!");
        }
    }

    @Data
    @Builder
    private static class CheckEmailResponse {
        Boolean taken;

        public static CheckEmailResponse available() {
            return new CheckEmailResponse(false);
        }

        public static CheckEmailResponse notAvailable() {
            return new CheckEmailResponse(true);
        }
    }

    @Data
    private static class AuthRequest {
        String uid;
        String password;
        String salt;
    }

    @Data
    private static class SignUpRequest {
        String email;
        String nickname;
        String password;
    }

    @Data
    @Builder
    private static class SignUpResponse {
        Boolean success;
        String message;
        Integer uid;

        public static SignUpResponse success(Integer uid) {
            return SignUpResponse.builder()
                .success(true)
                .message("Sign up success")
                .uid(uid)
                .build();
        }

        public static SignUpResponse fail(String message) {
            return SignUpResponse.builder()
                .success(false)
                .message(message)
                .build();
        }
    }

    @Data
    @Builder
    private static class AuthResponse {
        Boolean success;
        String message;
        String name;
        String token;

        public static AuthResponse success(String token, String name) {
            return new AuthResponseBuilder()
                .success(true)
                .message("Login success")
                .token(token)
                .name(name)
                .build();
        }

        public static AuthResponse fail(String message) {
            return new AuthResponseBuilder()
                .success(false)
                .message(message)
                .build();
        }
    }

    @Data
    private static class RenewRequest {
        Integer uid;
    }

    @Data
    @Builder
    private static class RenewResponse {
        Boolean success;
        String message;
        String token;

        public static RenewResponse success(String token) {
            return new RenewResponseBuilder()
                .success(true)
                .message("Renew success")
                .token(token)
                .build();
        }

        public static RenewResponse fail(String message) {
            return new RenewResponseBuilder()
                .success(false)
                .message(message)
                .build();
        }
    }
}
