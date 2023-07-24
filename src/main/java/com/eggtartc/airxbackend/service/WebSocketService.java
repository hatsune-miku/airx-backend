package com.eggtartc.airxbackend.service;

import com.eggtartc.airxbackend.entity.User;
import com.eggtartc.airxbackend.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@Service
public class WebSocketService extends TextWebSocketHandler {
    // Lookup table for user inet representation to user object
    private final ConcurrentHashMap<String, User> inetAddressUserMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<User, CopyOnWriteArrayList<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    private final Logger logger = Logger.getLogger("WebsocketService");

    @Resource
    private UserRepository userRepository;

    public void broadcastToAllLoggingPeers(User sender, byte[] payload) {
        CopyOnWriteArrayList<WebSocketSession> userSessions = sessions.get(sender);
        if (userSessions == null) {
            return;
        }

        // Send to all devices logging on the same AirX account.
        userSessions
            .forEach(session -> {
                try {
                    String representation = getAddressRepresentation(session).get();
                    logger.info("Sending to " + representation);
                    session.sendMessage(new BinaryMessage(payload));
                } catch (Exception ignored) {
                }
            });
    }

    private void onUserConnected(User user, WebSocketSession session) {
        logger.info("User " + user.getName() + " connected");
        CopyOnWriteArrayList<WebSocketSession> userSessions = sessions.get(user);
        if (userSessions == null) {
            userSessions = new CopyOnWriteArrayList<>();
            sessions.put(user, userSessions);
        }
        userSessions.add(session);
    }

    private void onUserConnectionClosed(User user, WebSocketSession session) {
        logger.info("User " + user.getName() + " disconnected");
        CopyOnWriteArrayList<WebSocketSession> userSessions = sessions.get(user);
        if (userSessions == null) {
            return;
        }
        userSessions.removeIf(s -> s.getId().equals(session.getId()));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Optional<String> representationOpt = getAddressRepresentation(session);
        if (representationOpt.isEmpty()) {
            return;
        }

        String representation = representationOpt.get();
        logger.info("Connection established: " + representation);

        User user = inetAddressUserMap.get(representation);
        if (user == null) {
            return;
        }

        onUserConnected(user, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Optional<String> representationOpt = getAddressRepresentation(session);
        if (representationOpt.isEmpty()) {
            return;
        }

        String representation = representationOpt.get();
        logger.info("Connection closed: " + representation);

        User user = inetAddressUserMap.get(representation);
        if (user == null) {
            return;
        }

        inetAddressUserMap.remove(representation);
        onUserConnectionClosed(user, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Optional<String> representationOpt = getAddressRepresentation(session);
        if (representationOpt.isEmpty()) {
            return;
        }

        String representation = representationOpt.get();

        try {
            int userUid = Integer.parseInt(message.getPayload());
            Optional<User> userOpt = userRepository.findByUid(userUid);

            if (userOpt.isEmpty()) {
                logger.warning("User " + userUid + " tried to register with " + representation);
                return;
            }

            User user = userOpt.get();

            logger.info("User " + user.getName() + " registered with " + representation);
            inetAddressUserMap.put(representation, user);

            // Trigger the event that should have been triggered
            afterConnectionEstablished(session);
        } catch (NumberFormatException ignored) {
        }
    }

    private Optional<String> getAddressRepresentation(WebSocketSession session) {
        String realIpAddress = (String) session.getAttributes().get("X-Real-IP");
        if (realIpAddress != null) {
            return Optional.of(realIpAddress);
        }

        InetSocketAddress address = session.getRemoteAddress();
        if (address != null) {
            return Optional.of(
                address.getHostName() + "@" + address.getAddress().toString() + ":" + address.getPort());
        }

        return Optional.empty();
    }
}
