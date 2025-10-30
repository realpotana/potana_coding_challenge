package com.example.instructions.service;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import com.example.instructions.util.TradeTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    
    private final TradeTransformer transformer;
    private final KafkaPublisher kafkaPublisher;
    private final ObjectMapper objectMapper;
    
    // In-memory storage for canonical trades
    private final ConcurrentHashMap<String, CanonicalTrade> canonicalTradeStore = new ConcurrentHashMap<>();

    @Autowired
    public TradeService(TradeTransformer transformer, KafkaPublisher kafkaPublisher, ObjectMapper objectMapper) {
        this.transformer = transformer;
        this.kafkaPublisher = kafkaPublisher;
        this.objectMapper = objectMapper;
    }

    public String processTrade(CanonicalTrade trade) {
        try {
            // Generate unique ID for tracking
            String tradeId = UUID.randomUUID().toString();
            
            // Normalize the trade
            CanonicalTrade normalizedTrade = transformer.normalize(trade);
            
            // Store in memory for auditing
            canonicalTradeStore.put(tradeId, normalizedTrade);
            
            // Transform to platform format
            PlatformTrade platformTrade = transformer.toPlatformFormat(normalizedTrade);
            
            // Convert to JSON and publish
            String jsonPayload = objectMapper.writeValueAsString(platformTrade);
            kafkaPublisher.publishTrade(jsonPayload);
            
            logger.info("Successfully processed trade with ID: {}", tradeId);
            return tradeId;
            
        } catch (Exception e) {
            logger.error("Error processing trade: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process trade", e);
        }
    }

    public CanonicalTrade getCanonicalTrade(String tradeId) {
        return canonicalTradeStore.get(tradeId);
    }

    public int getStoredTradeCount() {
        return canonicalTradeStore.size();
    }

    public void clearStorage() {
        canonicalTradeStore.clear();
    }
}