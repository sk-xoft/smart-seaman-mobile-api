package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentCreateResponse {
    private String documentCode;
    private String certStartDate;
    private String certEndDateType;
    private String certEndDate;
    private String fileCertName;
}
