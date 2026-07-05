package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class DocumentRequestItemUploadResponse {
    private String profileRequestItemId;
    private String itemCode;
    private String documentType;
    private Boolean complete;
    private List<DocumentRequestItemFileResponse> files;
}
