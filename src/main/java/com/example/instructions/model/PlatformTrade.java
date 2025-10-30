package com.example.instructions.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class PlatformTrade {
    
    @JsonProperty("platform_id")
    private String platformId;
    
    private TradeDetails trade;

    public PlatformTrade() {}

    public PlatformTrade(String platformId, TradeDetails trade) {
        this.platformId = platformId;
        this.trade = trade;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public TradeDetails getTrade() {
        return trade;
    }

    public void setTrade(TradeDetails trade) {
        this.trade = trade;
    }

    public static class TradeDetails {
        private String account;
        private String security;
        private String type;
        private BigDecimal amount;
        private String timestamp;

        public TradeDetails() {}

        public TradeDetails(String account, String security, String type, 
                           BigDecimal amount, String timestamp) {
            this.account = account;
            this.security = security;
            this.type = type;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getSecurity() {
            return security;
        }

        public void setSecurity(String security) {
            this.security = security;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}