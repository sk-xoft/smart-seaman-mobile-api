package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class DocumentRenewalSummaryEntity {
    private String requestId;
    private String requestNo;
    private String documentCode;
    private String documentNameTh;
    private String documentNameEn;
    private String statusId;
    private String statusCode;
    private String statusNameTh;
    private String statusNameEn;
    private String statusCssColor;
    private String documentMobileStatusCode;
    private String documentMobileStatusNameTh;
    private String documentMobileStatusNameEn;
    private Date submittedAt;
    private BigDecimal amount;
    private Boolean isResubmit;
}
