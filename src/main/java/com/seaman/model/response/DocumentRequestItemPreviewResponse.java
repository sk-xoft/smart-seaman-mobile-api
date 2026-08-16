package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentRequestItemPreviewResponse {
    private String documentMasterRequestItemCode;
    private String documentType;
    private List<DocumentRequestItemPreviewFileResponse> files;
}
