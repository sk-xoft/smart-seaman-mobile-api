package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentRenewalStageResponse {
    private String requestNo;
    private DocumentRenewalSummaryStatusResponse currentStatus;
    private List<DocumentRenewalStageItemResponse> stages;
}
