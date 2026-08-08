package com.seaman.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentRenewalDeleteResponse {
    private String requestNo;
    private String fromStatus;
    private String toStatus;
    private String action;
}
