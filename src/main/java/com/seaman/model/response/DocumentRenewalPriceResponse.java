package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DocumentRenewalPriceResponse {
    @JsonIgnore
    private String priceSettingId;
    private String documentCode;
    private BigDecimal governmentFee;
    private BigDecimal documentProcessingFee;
    private BigDecimal shippingFee;
    private BigDecimal shippingDiscount;
    private BigDecimal serviceFeeDiscount;
    private BigDecimal total;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
