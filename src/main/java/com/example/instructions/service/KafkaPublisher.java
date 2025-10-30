package com.example.instructions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topics.outbound}")
    private String outboundTopic;

    @Autowired
    public KafkaPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTrade(String tradeJson) {
        try {
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(outboundTopic, tradeJson);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.info("Successfully published trade to topic: {} with offset: {}", 
                              outboundTopic, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish trade to topic: {}", outboundTopic, exception);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing trade to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish trade", e);
        }
    }
}