package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class DocumentRequestValidateResponse {
    private String requestId;
    private String requestNo;
    private String documentCode;
    private String documentName;
    private String documentNameTh;
    private String documentNameEn;
    private String certEndDate;
    private String idempotencyKey;
    private String mobileNumber;
    private String email;
    private List<DeliveryAddressResponse> address = Collections.emptyList();
    private List<DocumentRequestItemResponse> items;
}
