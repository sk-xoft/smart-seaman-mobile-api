package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter @Setter
public class PaymentTransactionEntity {
    private String id;
    private String requestId;
    private String requestNo;
    private String parentTransactionId;
    private String transactionNo;
    private String transactionType;
    private String channel;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private BigDecimal refundedAmount;
    private String status;
    private String provider;
    private String providerChargeId;
    private String providerSourceId;
    private String providerRefundId;
    private String providerTransactionId;
    private String providerStatus;
    private String providerResponse;
    private String idempotencyKey;
    private String description;
    private String returnUri;
    private String authorizeUri;
    private String bankCode;
    private String cardBrand;
    private String cardLastDigits;
    private String failureCode;
    private String failureMessage;
    private Boolean isLivemode;
    private Date expiresAt;
    private Date paidAt;
    private Date failedAt;
    private Date refundedAt;
    private Date createdAt;
    private Date updatedAt;
}
