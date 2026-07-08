package com.seaman.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryAddressResponse {
    @Schema(example = "11111111-2222-3333-4444-555555555555")
    private String id;
    @Schema(example = "ศรัญญู")
    private String firstName;
    @Schema(example = "แก้วโสภา")
    private String lastName;
    @Schema(example = "16 ม. 8")
    private String addressLine;
    @Schema(example = "39")
    private String province;
    @Schema(example = "3902")
    private String district;
    @Schema(example = "390202")
    private String subDistrict;
    @Schema(example = "39170")
    private String postalCode;
    @Schema(example = "0812345678")
    private String mobileNumber;
    @Schema(example = "true")
    private Boolean isDefault;
}
