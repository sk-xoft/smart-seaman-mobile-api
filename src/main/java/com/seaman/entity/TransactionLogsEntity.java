package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class TransactionLogsEntity extends CommonEntity {

    private String clientSessionId;
    private String correlationId;
    private String transId;
    private String requestBy;
    private String serviceName;
    private String language;
    private String deviceModel;
    private String deviceInfo;
    private String token;
    private String requestData;
    private Date requestDateTime;
    private String responseStatusCode;
    private String responseStatusMessage;
    private String responseData;
    private Date responseDateTime;
}
