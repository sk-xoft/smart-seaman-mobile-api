package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter @Setter
public class PaymentTransactionEntity {
    private String id;
    private String requestId;
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
    private String idempotencyKey;
    private Date expiresAt;
    private Date paidAt;
    private Date createdAt;
    private Date updatedAt;
}
