package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter @Setter
public class DocumentRenewalRequestEntity {
    private String id;
    private String requestNo;
    private String mobileUserUuid;
    private String documentCode;
    private String documentStatusId;
    private String statusNameEn;
    private Boolean isResubmit;
    private BigDecimal amount;
    private Date submittedAt;
    private String submittedBy;
    private Date createdAt;
    private Date updatedAt;
}
