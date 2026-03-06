package com.seaman.model.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ProfileUserActivateRequest {
    @NotBlank(message =  "email")
    private String email;
}
