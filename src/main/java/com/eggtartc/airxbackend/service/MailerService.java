package com.eggtartc.airxbackend.service;

import com.eggtartc.airxbackend.entity.User;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;

@Service
public class MailerService {
    @Resource
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String sender;

    @Value("#{environment.getProperty('debug') != null && environment.getProperty('debug') != 'false'}")
    boolean isDebug;

    @Value("${server.port}")
    String port;

    public void sendActivationEmailAsync(User user) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(sender);
        mail.setTo(user.getEmail());
        mail.setSubject("Activate Your AirX Account");
        mail.setText(
            "(UID: " + user.getUid() + ") Welcome to AirX Cloud!\n" +
                "Please click the following link to activate your account: \n\n" +
                getBackendBaseUrl() + "/auth/activate/" + user.getSalt()
        );
        Executors.newFixedThreadPool(1).submit(() -> mailSender.send(mail));
    }

    private String getBackendBaseUrl() {
        return isDebug
            ? ("http://localhost:" + port)
            : "https://airx.eggtartc.com";
    }
}
