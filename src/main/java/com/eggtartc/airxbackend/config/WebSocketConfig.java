package com.eggtartc.airxbackend.config;

import com.eggtartc.airxbackend.config.interceptor.IpHandshakeInterceptor;
import com.eggtartc.airxbackend.service.WebSocketService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Resource
    WebSocketService webSocketService;

    @Resource
    IpHandshakeInterceptor ipHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketService, "/device-register")
            .setAllowedOriginPatterns("*")
            .addInterceptors(ipHandshakeInterceptor);
    }
}
