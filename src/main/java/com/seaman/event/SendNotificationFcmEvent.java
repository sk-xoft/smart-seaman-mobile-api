package com.seaman.event;

import com.seaman.model.external.request.FcmMessageRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.List;

@Getter
public class SendNotificationFcmEvent extends ApplicationEvent {

    private final List<String> deviceTokens;
    private final FcmMessageRequest request;

    public SendNotificationFcmEvent(Object source, List<String> deviceTokens, FcmMessageRequest request) {
        super(source);
        this.deviceTokens = deviceTokens;
        this.request = request;
    }
}
