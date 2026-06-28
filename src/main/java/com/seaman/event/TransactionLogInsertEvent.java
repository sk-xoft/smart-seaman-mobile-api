package com.seaman.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransactionLogInsertEvent extends ApplicationEvent {

    private final String transId;
    private final String serviceName;
    private final String requestData;
    private final String createBy;
    private final String language;
    private final String deviceModel;
    private final String deviceInfo;
    private final String correlationId;
    private final String token;
    private final String clientSessionId;

    public TransactionLogInsertEvent(Object source, String transId, String serviceName,
                                     String requestData, String createBy, String language,
                                     String deviceModel, String deviceInfo, String correlationId,
                                     String token, String clientSessionId) {
        super(source);
        this.transId = transId;
        this.serviceName = serviceName;
        this.requestData = requestData;
        this.createBy = createBy;
        this.language = language;
        this.deviceModel = deviceModel;
        this.deviceInfo = deviceInfo;
        this.correlationId = correlationId;
        this.token = token;
        this.clientSessionId = clientSessionId;
    }
}
