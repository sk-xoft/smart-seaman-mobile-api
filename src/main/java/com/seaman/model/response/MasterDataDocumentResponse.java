package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class MasterDataDocumentResponse {
    private List<DocumentResponse> documents;
    private List<DocumentResponse> cot;
}
