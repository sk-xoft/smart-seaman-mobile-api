package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DocumentUpdateResponse {
    private String documentCode;
    private String certStartDate;
    private String certEndDateType;
    private String certEndDate;
    private String fileCertName;
}
