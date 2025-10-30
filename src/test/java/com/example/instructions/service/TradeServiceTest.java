package com.example.instructions.service;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import com.example.instructions.util.TradeTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeTransformer transformer;

    @Mock
    private KafkaPublisher kafkaPublisher;

    @Mock
    private ObjectMapper objectMapper;

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        tradeService = new TradeService(transformer, kafkaPublisher, objectMapper);
    }

    @Test
    void testProcessTrade_Success() throws Exception {
        // Given
        CanonicalTrade inputTrade = new CanonicalTrade(
            "1234567890", "ABC123", "Buy", 
            BigDecimal.valueOf(100000), LocalDateTime.now()
        );
        
        CanonicalTrade normalizedTrade = new CanonicalTrade(
            "****7890", "ABC123", "B", 
            BigDecimal.valueOf(100000), LocalDateTime.now()
        );
        
        PlatformTrade platformTrade = new PlatformTrade("ACCT123", 
            new PlatformTrade.TradeDetails("****7890", "ABC123", "B", 
                BigDecimal.valueOf(100000), "2025-08-04T21:15:33Z"));

        when(transformer.normalize(inputTrade)).thenReturn(normalizedTrade);
        when(transformer.toPlatformFormat(normalizedTrade)).thenReturn(platformTrade);
        when(objectMapper.writeValueAsString(platformTrade)).thenReturn("{\"platform_id\":\"ACCT123\"}");

        // When
        String tradeId = tradeService.processTrade(inputTrade);

        // Then
        assertNotNull(tradeId);
        verify(transformer).normalize(inputTrade);
        verify(transformer).toPlatformFormat(normalizedTrade);
        verify(kafkaPublisher).publishTrade(anyString());
        assertEquals(1, tradeService.getStoredTradeCount());
    }

    @Test
    void testGetCanonicalTrade() {
        // Given
        CanonicalTrade trade = new CanonicalTrade(
            "1234567890", "ABC123", "Buy", 
            BigDecimal.valueOf(100000), LocalDateTime.now()
        );
        
        when(transformer.normalize(any())).thenReturn(trade);
        when(transformer.toPlatformFormat(any())).thenReturn(mock(PlatformTrade.class));

        // When
        String tradeId = tradeService.processTrade(trade);
        CanonicalTrade retrieved = tradeService.getCanonicalTrade(tradeId);

        // Then
        assertNotNull(retrieved);
        assertEquals(trade.getAccountNumber(), retrieved.getAccountNumber());
    }

    @Test
    void testClearStorage() {
        // Given
        CanonicalTrade trade = new CanonicalTrade(
            "1234567890", "ABC123", "Buy", 
            BigDecimal.valueOf(100000), LocalDateTime.now()
        );
        
        when(transformer.normalize(any())).thenReturn(trade);
        when(transformer.toPlatformFormat(any())).thenReturn(mock(PlatformTrade.class));

        tradeService.processTrade(trade);
        assertEquals(1, tradeService.getStoredTradeCount());

        // When
        tradeService.clearStorage();

        // Then
        assertEquals(0, tradeService.getStoredTradeCount());
    }
}