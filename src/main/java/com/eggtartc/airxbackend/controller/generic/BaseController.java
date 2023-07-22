package com.eggtartc.airxbackend.controller.generic;

import com.eggtartc.airxbackend.entity.FileShare;
import com.eggtartc.airxbackend.entity.User;
import com.eggtartc.airxbackend.repository.FileRepository;
import com.eggtartc.airxbackend.repository.FileShareRepository;
import com.eggtartc.airxbackend.repository.FileStoreRepository;
import com.eggtartc.airxbackend.repository.UserRepository;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Instant;
import java.util.Optional;

public class BaseController {
    @Resource
    protected UserRepository userRepository;

    @Resource
    protected FileRepository fileRepository;

    @Resource
    protected FileShareRepository fileShareRepository;

    @Resource
    protected FileStoreRepository fileStoreRepository;

    @Resource
    protected JwtEncoder jwtEncoder;

    @Resource
    protected RedisTemplate<String, String> redisTemplate;

    @Resource
    protected RedisTemplate<String, Integer> redisTemplateInteger;

    protected Optional<User> getUserFromJwtToken(Jwt token) {
        String userUid = token.getClaimAsString("userUid");
        Optional<User> userOpt = userRepository.findByUid(Integer.parseInt(userUid));
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();

        if (!token.getClaimAsString("name").equals(user.getName())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
