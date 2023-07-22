package com.eggtartc.airxbackend.helper;

import org.springframework.kafka.core.KafkaTemplate;

public class KafkaHelper<T> {
    private final KafkaTemplate<String, T> kafka;

    public KafkaHelper(KafkaTemplate<String, T> kafka) {
        this.kafka = kafka;
    }

    public void produce(String topic, T message) {
        kafka.send(topic, message);
    }
}
