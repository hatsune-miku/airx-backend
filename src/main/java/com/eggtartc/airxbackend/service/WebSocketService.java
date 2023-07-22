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

        userSessions
            // Don't send to self
            .stream().filter(session -> session.getRemoteAddress() != null)
            // Send to all devices logging on the same AirX account.
            .forEach(session -> {
                try {
                    session.sendMessage(new BinaryMessage(payload));
                } catch (Exception ignored) {
                }
            });
    }

    private void onUserConnected(User user, WebSocketSession session) {
        CopyOnWriteArrayList<WebSocketSession> userSessions = sessions.get(user);
        if (userSessions == null) {
            userSessions = new CopyOnWriteArrayList<>();
            sessions.put(user, userSessions);
        }
        userSessions.add(session);
    }

    private void onUserConnectionClosed(User user, WebSocketSession session) {
        CopyOnWriteArrayList<WebSocketSession> userSessions = sessions.get(user);
        if (userSessions == null) {
            return;
        }
        userSessions.remove(session);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        InetSocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress == null) {
            return;
        }

        User user = inetAddressUserMap.get(getAddressRepresentation(remoteAddress));
        if (user == null) {
            return;
        }

        onUserConnected(user, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        InetSocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress == null) {
            return;
        }

        User user = inetAddressUserMap.get(getAddressRepresentation(remoteAddress));
        if (user == null) {
            return;
        }

        onUserConnectionClosed(user, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            int userUid = Integer.parseInt(message.getPayload());
            Optional<User> userOpt = userRepository.findByUid(userUid);
            InetSocketAddress remoteAddress = session.getRemoteAddress();

            if (userOpt.isEmpty() || remoteAddress == null) {
                logger.warning("User " + userUid + " tried to register with " + remoteAddress);
                return;
            }

            User user = userOpt.get();
            String representation = getAddressRepresentation(remoteAddress);

            logger.info("User " + user.getName() + " registered with " + representation);
            inetAddressUserMap.put(representation, user);

            // Trigger the event that should have been triggered
            afterConnectionEstablished(session);
        } catch (NumberFormatException ignored) {
        }
    }

    private String getAddressRepresentation(InetSocketAddress address) {
        return address.getHostName() + "@" + address.getAddress().toString() + ":" + address.getPort();
    }
}
