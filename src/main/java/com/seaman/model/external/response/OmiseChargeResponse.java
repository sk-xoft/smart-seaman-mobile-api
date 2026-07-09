package com.seaman.model.external.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OmiseChargeResponse {
    private String id;
    private String sourceId;
    private String status;
    private String authorizeUri;
    private String returnUri;
    private String qrCodeDownloadUri;
    private String failureCode;
    private String failureMessage;
    private String transactionId;
    private Boolean livemode;
    private Date expiresAt;
    private Date paidAt;
    private JsonNode raw;
}
