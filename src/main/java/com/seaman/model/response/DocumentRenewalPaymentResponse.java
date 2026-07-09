package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class DocumentRenewalPaymentResponse {
    private String requestId;
    private String transactionId;
    private String transactionNo;
    private String status;
    private String channel;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private String provider;
    private String providerChargeId;
    private String providerSourceId;
    private Date expiresAt;
    private Date paidAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String authorizeUri;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String qrCodeDownloadUri;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String failureCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String failureMessage;
}
