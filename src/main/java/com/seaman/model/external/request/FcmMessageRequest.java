package com.seaman.model.external.request;

import com.seaman.model.request.NotificationModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmMessageRequest {
    private String to;
    private String priority;
    private boolean mutable_content;
    private NotificationModel notification;
    private FcmMessageData data;
}
