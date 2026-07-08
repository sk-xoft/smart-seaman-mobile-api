package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class RenewalRequestItemEntity {
    private String id;
    private String requestId;
    private String requestNo;
    private String documentMasterRequestItemCode;
    private String approveStatus;
    private String note;
    private String statusNameEn;
    private Date createdAt;
    private Date updatedAt;
}
