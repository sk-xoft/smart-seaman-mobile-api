package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DocumentRenewalPriceEntity {
    private String documentCode;
    private BigDecimal governmentFee;
    private BigDecimal documentProcessingFee;
    private BigDecimal shippingFee;
    private BigDecimal shippingDiscount;
    private BigDecimal serviceFeeDiscount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
