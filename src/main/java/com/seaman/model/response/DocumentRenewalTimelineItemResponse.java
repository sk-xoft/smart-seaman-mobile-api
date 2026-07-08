package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalTimelineItemResponse {
    private String action;
    private String fromStatus;
    private String toStatus;
    private String actionedAt;
    private String detail;
}
