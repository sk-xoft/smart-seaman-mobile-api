package com.seaman.model.request;

import lombok.Data;
import javax.validation.constraints.NotEmpty;

@Data
public class SendNotificationRequest {
    @NotEmpty
    private String valueId;

    @NotEmpty
    private String notiType;
}
