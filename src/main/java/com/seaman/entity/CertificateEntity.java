package com.seaman.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class CertificateEntity extends CommonEntity{
    private String certId;
    private String certMobileUuid;
    private String certDocumentCode;
    private String certStartDate;
    private String certEndDate;
    private String certFile;
    private String originalFileName;
    private String certStatus;
}
