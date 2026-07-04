package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DocumentRenewalPriceResponse {
    private String documentCode;
    private BigDecimal governmentFee;
    private BigDecimal documentProcessingFee;
    private BigDecimal shippingFee;
    private BigDecimal shippingDiscount;
    private BigDecimal serviceFeeDiscount;
    private BigDecimal total;
}
