package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DocumentRequestItemFileResponse {
    private String fileId;
    private String documentType;
    private String slotCode;
    private String originalFileName;
    private String mimeType;
    private Long fileSize;
    private String fileUploadedAt;
    private String checkResult;
    private String checkNote;
    private Boolean isUpdated;
}
