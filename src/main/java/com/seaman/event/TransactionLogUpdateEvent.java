package com.seaman.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransactionLogUpdateEvent extends ApplicationEvent {

    private final String transId;
    private final String responseData;
    private final String statusCode;
    private final String statusMessage;
    private final String updateBy;

    public TransactionLogUpdateEvent(Object source, String transId, String responseData,
                                     String statusCode, String statusMessage, String updateBy) {
        super(source);
        this.transId = transId;
        this.responseData = responseData;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.updateBy = updateBy;
    }
}
