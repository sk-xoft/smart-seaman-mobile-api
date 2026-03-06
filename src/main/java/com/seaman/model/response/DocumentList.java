package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DocumentList {
    private String documentId;
    private String documentCode;
    private String documentNameTh;
    private String documentNameEn;
    private String documentFullNameEn;
    private String documentFullNameTh;
    private String documentDescription;
    private String documentType;
    private String documentDefaultFlag;
    private String documentSeq;
    private String documentMobileFlag;
    private String documentCompanyCode;
    private String documentPositionCode;
    private String documentStatus;
}
