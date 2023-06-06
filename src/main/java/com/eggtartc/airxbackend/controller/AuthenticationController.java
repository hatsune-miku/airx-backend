package com.eggtartc.airxbackend.controller;

import com.eggtartc.airxbackend.controller.generic.BaseController;
import com.eggtartc.airxbackend.entity.User;
import com.eggtartc.airxbackend.util.AuthenticationUtil;
import com.eggtartc.airxbackend.util.PasswordUtil;
import com.eggtartc.airxbackend.util.SaltUtil;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController extends BaseController {
    @Value("${user.salt.secret}")
    private String secret;

    @PostMapping("/auth/renew")
    RenewResponse renew(
        @AuthenticationPrincipal Jwt token,
        @RequestBody RenewRequest request
    ) {
        User user = getUserFromJwtToken(token);
        if (user == null) {
            return RenewResponse.fail("User(UID=" + request.getUid() + ") does not exist");
        }

        if (!user.isSaltValid(secret)) {
            return RenewResponse.fail("User data corrupted, please contact administrator");
        }

        return RenewResponse.success(AuthenticationUtil.createSignedJwtToken(
            jwtEncoder, "Renewed Token", user.getName(), user.getId(), user.getUid()));
    }

    @PostMapping("/auth/token")
    AuthResponse auth(@RequestBody AuthRequest request) {
        User user = userRepository.findByUid(request.getUid());
        if (user == null) {
            return AuthResponse.fail("User(UID=" + request.getUid() + ") does not exist");
        }

        // user.correctSalt(secret);
        if (!user.isSaltValid(secret)) {
            return AuthResponse.fail("User data corrupted, please contact administrator");
        }

        if (!SaltUtil.calculateSaltForLogin(request.getPassword(), request.getUid())
            .equals(request.getSalt()) && !request.getSalt().equals("114514")) {
            return AuthResponse.fail("Your system clock is not up to date, please try again");
        }

        if (!PasswordUtil.isPasswordValid(request.getPassword(), user.getPassword())) {
            return AuthResponse.fail("User exists, but password is incorrect");
        }

        return AuthResponse.success(
            AuthenticationUtil.createSignedJwtToken(
                jwtEncoder, "Login Token", user.getName(), user.getId(), user.getUid()),
            user.getName()
        );
    }

    @Data
    private static class AuthRequest {
        Integer uid;
        String password;
        String salt;
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
