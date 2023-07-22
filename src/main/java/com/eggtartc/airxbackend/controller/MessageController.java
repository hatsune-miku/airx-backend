package com.eggtartc.airxbackend.controller;

import com.eggtartc.airxbackend.controller.generic.BaseController;
import com.eggtartc.airxbackend.entity.User;
import com.eggtartc.airxbackend.enums.Topics;
import com.eggtartc.airxbackend.form.MessageSend;
import com.eggtartc.airxbackend.model.Message;
import com.eggtartc.airxbackend.serializer.MessageSerializer;
import com.eggtartc.airxbackend.service.WebSocketService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.logging.Logger;

@RestController
public class MessageController extends BaseController {
    @Resource
    private KafkaTemplate<String, Message> kafkaTemplate;

    @Resource
    private WebSocketService webSocketService;

    Logger logger = Logger.getLogger(this.getClass().getName());

    @PostMapping("/api/v1/message")
    public ResponseEntity<MessageSend.Response> sendMessage(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody MessageSend.Request request
    ) {
        Optional<User> userOpt = getUserFromJwtToken(jwt);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userOpt.get();

        Message.Type type;
        try {
            type = Message.Type.fromRawValue(request.getType());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        Message message = Message.builder()
            .senderUid(user.getUid())
            .type(type)
            .rawContent(request.getContent())
            .build();
        kafkaTemplate.send(Topics.SYNCHRONIZATION_MESSAGE, message);

        return ResponseEntity.ok(
            MessageSend.Response.builder()
                .success(true)
                .message("Message sent.")
                .build()
        );
    }

    @KafkaListener(topics = Topics.SYNCHRONIZATION_MESSAGE)
    public void onMessage(Message message) {
        Optional<User> userOpt = userRepository.findByUid(message.getSenderUid());
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();

        try (MessageSerializer serializer = new MessageSerializer()) {
            byte[] payload = serializer.serialize(null, message);
            webSocketService.broadcastToAllLoggingPeers(user, payload);
        } catch (Exception e) {
            logger.warning("Failed to serialize message.");
            e.printStackTrace();
        }
    }
}
