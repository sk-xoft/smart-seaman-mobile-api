package com.seaman.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentRenewalResubmittedEvent {
    private String mobileUserUuid;
    private String requestNo;
}
