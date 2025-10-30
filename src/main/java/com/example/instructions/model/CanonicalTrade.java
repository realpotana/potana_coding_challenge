package com.example.instructions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CanonicalTrade {
    
    @NotBlank
    @JsonProperty("account_number")
    private String accountNumber;
    
    @NotBlank
    @JsonProperty("security_id")
    private String securityId;
    
    @NotBlank
    @JsonProperty("trade_type")
    private String tradeType;
    
    @NotNull
    @Positive
    private BigDecimal amount;
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public CanonicalTrade() {}

    public CanonicalTrade(String accountNumber, String securityId, String tradeType, 
                         BigDecimal amount, LocalDateTime timestamp) {
        this.accountNumber = accountNumber;
        this.securityId = securityId;
        this.tradeType = tradeType;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}