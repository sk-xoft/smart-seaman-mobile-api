package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class FcmNotificationRequest {

    @NotBlank
    private String tokenFcm;
}
