package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentRenewalTimelineResponse {
    private String requestNo;
    private List<DocumentRenewalTimelineItemResponse> items;
}
