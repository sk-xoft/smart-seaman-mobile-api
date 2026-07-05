package com.seaman.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentRenewalStatus {
    PENDING_DOCUMENT_REVIEW("Pending Document Review"),
    PENDING_APPLICANT_CORRECTION("Pending Applicant Correction"),
    PENDING_MARINE_DEPARTMENT_RESULT("Pending Marine Department Result"),
    PENDING_DEPARTMENT_DOCUMENT_PICKUP("Pending Department Document Pickup"),
    DELIVERING("Delivering"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String masterNameEn;
}
