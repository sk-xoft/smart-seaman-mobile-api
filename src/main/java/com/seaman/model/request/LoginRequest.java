package com.seaman.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.seaman.validate.StringOnlyDeserializer;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class LoginRequest {
    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String username;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String password;
}
