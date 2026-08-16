package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class DocumentRenewalTransactionEntity {
    private String id;
    private String requestId;
    private String action;
    private String fromStatusNameTh;
    private String fromStatusNameEn;
    private String toStatusNameTh;
    private String toStatusNameEn;
    private String note;
    private Date actionedAt;
    private String actionedBy;
}
