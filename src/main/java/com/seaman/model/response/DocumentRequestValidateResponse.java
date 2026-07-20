package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentRequestValidateResponse {
    private String requestId;
    private String requestNo;
    private String documentCode;
    private List<DocumentRequestItemResponse> items;
}
