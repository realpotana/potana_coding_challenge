package com.example.instructions.util;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TradeTransformerTest {

    private TradeTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new TradeTransformer();
        ReflectionTestUtils.setField(transformer, "platformId", "ACCT123");
    }

    @Test
    void testNormalize_MasksAccountNumber() {
        CanonicalTrade trade = new CanonicalTrade(
            "1234567890", "ABC123", "Buy", 
            BigDecimal.valueOf(100000), LocalDateTime.now()
        );

        CanonicalTrade normalized = transformer.normalize(trade);

        assertEquals("****7890", normalized.getAccountNumber());
    }

    @Test
    void testNormalize_ConvertsSecurityIdToUppercase() {
        CanonicalTrade trade = new CanonicalTrade(
            "1234567890", "abc123", "Buy", 
            BigDecimal.valueOf(100000), LocalDateTime.now()
        );

        CanonicalTrade normalized = transformer.normalize(trade);

        assertEquals("ABC123", normalized.getSecurityId());
    }

    @Test
    void testNormalize_NormalizesTradeType() {
        CanonicalTrade trade = new CanonicalTrade(
            "1234567890", "ABC123", "Buy", 
            BigDecimal.valueOf(100000), LocalDateTime.now()
        );

        CanonicalTrade normalized = transformer.normalize(trade);

        assertEquals("B", normalized.getTradeType());
    }

    @Test
    void testToPlatformFormat() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 8, 4, 21, 15, 33);
        CanonicalTrade canonical = new CanonicalTrade(
            "****7890", "ABC123", "B", 
            BigDecimal.valueOf(100000), timestamp
        );

        PlatformTrade platform = transformer.toPlatformFormat(canonical);

        assertEquals("ACCT123", platform.getPlatformId());
        assertEquals("****7890", platform.getTrade().getAccount());
        assertEquals("ABC123", platform.getTrade().getSecurity());
        assertEquals("B", platform.getTrade().getType());
        assertEquals(BigDecimal.valueOf(100000), platform.getTrade().getAmount());
        assertEquals("2025-08-04T21:15:33Z", platform.getTrade().getTimestamp());
    }

    @Test
    void testNormalize_InvalidSecurityId_ThrowsException() {
        CanonicalTrade trade = new CanonicalTrade(
            "1234567890", "INVALID", "Buy", 
            BigDecimal.valueOf(100000), LocalDateTime.now()
        );

        assertThrows(IllegalArgumentException.class, () -> transformer.normalize(trade));
    }
}