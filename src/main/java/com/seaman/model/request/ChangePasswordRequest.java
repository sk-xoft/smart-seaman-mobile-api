package com.seaman.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.seaman.validate.StringOnlyDeserializer;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class ChangePasswordRequest {
    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String oldPassword;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String newPassword;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String confirmPassword;
}
