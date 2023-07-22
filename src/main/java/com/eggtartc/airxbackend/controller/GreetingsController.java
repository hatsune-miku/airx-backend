package com.eggtartc.airxbackend.controller;

import com.eggtartc.airxbackend.controller.generic.BaseController;
import com.eggtartc.airxbackend.entity.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Optional;

@RestController
public class GreetingsController extends BaseController {
    @GetMapping("/api/v1/greetings")
    GreetingsResponse greetings(@AuthenticationPrincipal Jwt token) {
        Optional<User> userOpt = getUserFromJwtToken(token);
        if (userOpt.isEmpty()) {
            return GreetingsResponse.fail("User does not exist");
        }
        User user = userOpt.get();

        return GreetingsResponse.builder()
            .success(true)
            .message("Hello, " + user.getName() + "!")
            .name(user.getName())
            .uid(user.getUid())
            .build();
    }


    @Data
    @Builder
    private static class GreetingsResponse {
        private Boolean success;
        private String message;
        private String name;
        private Integer uid;

        public static GreetingsResponse fail(String message) {
            return GreetingsResponse.builder()
                .success(false)
                .message(message)
                .build();
        }
    }
}
