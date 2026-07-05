package com.seaman.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentRenewalTransitionResponse {
    private String requestId;
    private String fromStatus;
    private String toStatus;
    private String action;
}
