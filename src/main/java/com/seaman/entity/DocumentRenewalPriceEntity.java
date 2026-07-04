package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DocumentRenewalPriceEntity {
    private String documentCode;
    private BigDecimal governmentFee;
    private BigDecimal documentProcessingFee;
    private BigDecimal shippingFee;
    private BigDecimal shippingDiscount;
    private BigDecimal serviceFeeDiscount;
}
