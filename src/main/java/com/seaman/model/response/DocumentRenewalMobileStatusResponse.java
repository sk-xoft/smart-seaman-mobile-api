package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalMobileStatusResponse {
    private String documentMobileStatusCode;
    private String nameTh;
    private String nameEn;
    private Integer step;
}
