package com.seaman.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DocumentRenewalPaymentSucceededEvent {
    private final String mobileUserUuid;
    private final String requestNo;
}
