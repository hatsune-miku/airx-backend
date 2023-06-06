package com.eggtartc.airxbackend.util;

import com.eggtartc.airxbackend.security.JwtCrypt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;

public class AuthenticationUtil {

    public static String createSignedJwtToken(
        JwtEncoder encoder,
        String subject,
        String name,
        Integer userId,
        Integer userUid
    ) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(3600).plusSeconds(360000000);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("EGGTARTc")
            .issuedAt(now)
            .expiresAt(expireAt)
            .subject(subject)
            .claim("name", name)
            .claim("userId", userId.toString())
            .claim("userUid", userUid.toString())
            .build();
        String encodedToken = encoder.encode(
            JwtEncoderParameters.from(claims)
        ).getTokenValue();
        return JwtCrypt.encrypt(encodedToken);
    }

}
