package com.example.instructions.util;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TradeTransformer {

    @Value("${app.platform.id}")
    private String platformId;

    private static final Map<String, String> TRADE_TYPE_MAPPING = Map.of(
        "BUY", "B",
        "SELL", "S",
        "Buy", "B",
        "Sell", "S"
    );

    private static final Pattern SECURITY_ID_PATTERN = Pattern.compile("^[A-Z0-9]{6}$");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public CanonicalTrade normalize(CanonicalTrade trade) {
        // Mask account number - show only last 4 digits
        String maskedAccount = maskAccountNumber(trade.getAccountNumber());
        
        // Normalize security ID to uppercase and validate
        String normalizedSecurity = normalizeSecurityId(trade.getSecurityId());
        
        // Normalize trade type
        String normalizedTradeType = normalizeTradeType(trade.getTradeType());

        return new CanonicalTrade(
            maskedAccount,
            normalizedSecurity,
            normalizedTradeType,
            trade.getAmount(),
            trade.getTimestamp()
        );
    }

    public PlatformTrade toPlatformFormat(CanonicalTrade canonical) {
        PlatformTrade.TradeDetails details = new PlatformTrade.TradeDetails(
            canonical.getAccountNumber(),
            canonical.getSecurityId(),
            canonical.getTradeType(),
            canonical.getAmount(),
            canonical.getTimestamp().format(TIMESTAMP_FORMATTER)
        );

        return new PlatformTrade(platformId, details);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }

    private String normalizeSecurityId(String securityId) {
        if (securityId == null) {
            throw new IllegalArgumentException("Security ID cannot be null");
        }
        
        String normalized = securityId.toUpperCase().trim();
        
        if (!SECURITY_ID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid security ID format: " + securityId);
        }
        
        return normalized;
    }

    private String normalizeTradeType(String tradeType) {
        if (tradeType == null) {
            throw new IllegalArgumentException("Trade type cannot be null");
        }
        
        return TRADE_TYPE_MAPPING.getOrDefault(tradeType.trim(), tradeType.toUpperCase());
    }
}