package com.example.instructions.service;

import com.example.instructions.model.CanonicalTrade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaListenerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListenerService.class);

    private final TradeService tradeService;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaListenerService(TradeService tradeService, ObjectMapper objectMapper) {
        this.tradeService = tradeService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.inbound}")
    public void handleTradeInstruction(@Payload String message,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                     @Header(KafkaHeaders.OFFSET) long offset,
                                     Acknowledgment acknowledgment) {
        
        logger.info("Received message from topic: {}, partition: {}, offset: {}", 
                   topic, partition, offset);
        
        try {
            // Parse the incoming message as CanonicalTrade
            CanonicalTrade trade = objectMapper.readValue(message, CanonicalTrade.class);
            
            // Process the trade
            String tradeId = tradeService.processTrade(trade);
            
            logger.info("Successfully processed Kafka trade message with ID: {}", tradeId);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Error processing Kafka message: {}", e.getMessage(), e);
            // In production, you might want to send to a dead letter queue
            // For now, we'll acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}