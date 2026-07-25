package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentRequestItemFileResponse {
    private String fileId;
    private String documentType;
    private String slotCode;
    private String filePath;
    private String originalFileName;
    private String mimeType;
    private Long fileSize;
    private String fileUploadedAt;
    private String checkResult;
    private String checkNote;
    private Boolean isUpdated;
}
