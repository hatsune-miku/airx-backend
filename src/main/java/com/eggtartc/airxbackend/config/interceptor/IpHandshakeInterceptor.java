package com.eggtartc.airxbackend.config.interceptor;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.logging.Logger;

@Component
public class IpHandshakeInterceptor implements HandshakeInterceptor {
    Logger logger = Logger.getLogger("IpHandshakeInterceptor");

    @Override
    public boolean beforeHandshake(
        @NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,
        @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes
    ) throws Exception {
        // Set ip attribute to WebSocket session
        logger.info("Setting X-Real-IP attribute to WebSocket session");
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String xRealIp = servletRequest.getServletRequest().getHeader("X-Real-IP");
            if (xRealIp != null) {
                logger.info("Setting X-Real-IP attribute to WebSocket session: " + xRealIp);
                attributes.put("X-Real-IP", xRealIp);
            } else {
                logger.info("X-Real-IP header not found. Falling back to remote address: " + request.getRemoteAddress());
                attributes.put("X-Real-IP", request.getRemoteAddress());
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(
        @NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,
        @NotNull WebSocketHandler wsHandler, Exception exception
    ) {
    }
}
