package com.seaman.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DocumentUploadRequest {

    // File Image Base64
    private String certFile;
}
