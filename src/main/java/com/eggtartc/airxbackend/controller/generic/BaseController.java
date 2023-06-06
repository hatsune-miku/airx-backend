package com.eggtartc.airxbackend.controller.generic;

import com.eggtartc.airxbackend.entity.User;
import com.eggtartc.airxbackend.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Instant;

public class BaseController {
    @Resource
    protected UserRepository userRepository;

    @Resource
    protected JwtEncoder jwtEncoder;

    @Resource
    protected RedisTemplate<String, String> redisTemplate;

    protected User getUserFromJwtToken(Jwt token) {
        String userUid = token.getClaimAsString("userUid");
        User user = userRepository.findByUid(Integer.parseInt(userUid));
        if (!token.getClaimAsString("name").equals(user.getName())) {
            return null;
        }
        return user;
    }
}
