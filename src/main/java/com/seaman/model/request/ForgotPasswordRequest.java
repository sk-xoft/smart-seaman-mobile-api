package com.seaman.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.seaman.validate.StringOnlyDeserializer;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ForgotPasswordRequest {
    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String password;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String confirmPassword;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String uid;

}
