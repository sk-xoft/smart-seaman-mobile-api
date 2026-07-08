package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DocumentRenewalSummaryResponse {
    private String requestId;
    private String requestNo;
    private String documentCode;
    private String documentName;
    private DocumentRenewalSummaryStatusResponse status;
    private String submittedAt;
    private BigDecimal amount;
    private Boolean isResubmit;
}
