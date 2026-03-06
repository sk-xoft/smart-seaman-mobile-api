package com.seaman.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.seaman.validate.StringOnlyDeserializer;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class DocumentCreateRequest {

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String documentCode;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String certStartDate;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String certEndDateType;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String certEndDate;

    private String fileCert;

    private String fileCertName;
}
