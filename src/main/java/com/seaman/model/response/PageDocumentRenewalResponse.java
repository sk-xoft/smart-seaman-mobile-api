package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageDocumentRenewalResponse {
    private int itemTotal;
    private boolean isLast;
    private List<DocumentRenewalSummaryResponse> items;
}
