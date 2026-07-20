package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalStatusResponse {
    private String id;
    private String documentStatusCode;
    private String nameTh;
    private String nameEn;
    private String cssColor;
    private Integer progressStep;
    private boolean correction;
    private boolean terminal;
}
