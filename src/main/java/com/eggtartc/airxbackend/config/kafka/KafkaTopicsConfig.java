package com.eggtartc.airxbackend.config.kafka;

import com.eggtartc.airxbackend.enums.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
    @Bean
    public NewTopic textMessageTopic() {
        return TopicBuilder.name(Topics.SYNCHRONIZATION_MESSAGE).build();
    }
}
