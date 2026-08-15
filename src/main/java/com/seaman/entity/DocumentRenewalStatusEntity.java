package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalStatusEntity {
    private String id;
    private String documentStatusCode;
    private String documentMobileStatusCode;
    private String documentMobileStatusNameTh;
    private String documentMobileStatusNameEn;
    private String nameTh;
    private String nameEn;
    private String cssColor;
}
