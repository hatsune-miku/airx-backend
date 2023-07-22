package com.eggtartc.airxbackend.controller;

import com.eggtartc.airxbackend.security.JwtCrypt;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
    @PostMapping("/debug/decrypt-token")
    public DecryptTokenResponse decryptToken(@RequestBody DecryptTokenRequest request) {
        return DecryptTokenResponse.builder()
            .token(JwtCrypt.decrypt(request.token))
            .build();
    }

    @Data
    private static class DecryptTokenRequest {
        String token;
    };

    @Data
    @Builder
    private static class DecryptTokenResponse {
        String token;
    }
}
