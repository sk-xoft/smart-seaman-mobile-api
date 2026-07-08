package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Setter
@Getter
public class ProfileRequest {

    @NotBlank(message = "First name")
    private String firstName;

    @NotBlank(message =  "Last name")
    private String lastName;

    private String companyCode;

    private String dateOfBirth;

    @NotBlank(message =  "Position")
    private String positionCode;

    @NotBlank(message =  "Email")
    private String email;

    @NotBlank(message = "Mobile number")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Mobile number")
    private String mobileNumber;

    private String isChangeFile;

    private String imageProfile;
}
