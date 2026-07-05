package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentRequestItemResponse {

    private String id;
    private String profileRequestItemId;
    private String documentCode;
    private String documentMasterRequestItemCode;
    private String documentType;
    private String documentName;
    private String documentStatus;
    private Integer sortOrder;
    private Integer fileUploaded;
    private String filePath;
    private String fileUploadedAt;
    private String checkResult;
    private String checkNote;
    private java.util.List<DocumentRequestItemFileResponse> files;
}
