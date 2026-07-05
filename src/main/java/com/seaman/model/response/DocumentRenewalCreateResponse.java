package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DocumentRenewalCreateResponse {
    private String requestId;
    private String requestNo;
    private String documentCode;
    private String status;
    private BigDecimal amount;
    private String deliveryAddressId;
}
