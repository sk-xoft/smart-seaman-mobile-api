package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThailandAddressResponse {

    private String code;
    private String name;
    private String nameTh;
    private String nameEn;
    private String postalCode;
}
