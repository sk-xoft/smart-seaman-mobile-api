package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentEntity {

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

    // Custom Join Certification
    private String certStartDate;
    private String certEndDate;
    private String certFile;
    private String certFileName;

    // API Calculate
    private String disYear;
    private String disMonth;
    private String disDay;

    private String documentCourseCode;
}
