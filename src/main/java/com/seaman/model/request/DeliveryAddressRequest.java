package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class DeliveryAddressRequest {
    @NotBlank(message = "firstName")
    @Size(max = 100, message = "firstName")
    @Schema(example = "ศรัญญู")
    private String firstName;

    @NotBlank(message = "lastName")
    @Size(max = 100, message = "lastName")
    @Schema(example = "แก้วโสภา")
    private String lastName;

    @NotBlank(message = "addressLine")
    @Size(max = 500, message = "addressLine")
    @Schema(example = "16 ม. 8")
    private String addressLine;

    @NotBlank(message = "province")
    @Size(max = 100, message = "province")
    @Schema(description = "Province code", example = "39")
    private String province;

    @NotBlank(message = "district")
    @Size(max = 100, message = "district")
    @Schema(description = "District code", example = "3902")
    private String district;

    @NotBlank(message = "subDistrict")
    @Size(max = 100, message = "subDistrict")
    @Schema(description = "Subdistrict code", example = "390202")
    private String subDistrict;

    @NotBlank(message = "postalCode")
    @Pattern(regexp = "^[0-9]{5}$", message = "postalCode")
    @Schema(example = "39170")
    private String postalCode;

    @NotNull(message = "isDefault")
    @Schema(example = "true")
    private Boolean isDefault;
}
