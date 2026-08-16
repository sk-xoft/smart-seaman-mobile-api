package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalStageItemResponse {
    private Integer step;
    private String documentMobileStatusCode;
    private String nameTh;
    private String nameEn;
    private String state;
}
