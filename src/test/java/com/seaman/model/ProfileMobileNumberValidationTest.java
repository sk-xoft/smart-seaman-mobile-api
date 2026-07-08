package com.seaman.model;

import com.seaman.model.request.ProfileRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileMobileNumberValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void acceptsThaiTenDigitMobileNumber() {
        assertTrue(validator.validate(request("0812345678")).isEmpty());
    }

    @Test
    void rejectsBlankNonNumericAndWrongLengthNumbers() {
        assertFalse(validator.validate(request(" ")).isEmpty());
        assertFalse(validator.validate(request("08A2345678")).isEmpty());
        assertFalse(validator.validate(request("081234567")).isEmpty());
    }

    private ProfileRequest request(String mobileNumber) {
        ProfileRequest request = new ProfileRequest();
        request.setFirstName("First");
        request.setLastName("Last");
        request.setPositionCode("POS001");
        request.setEmail("user@example.com");
        request.setMobileNumber(mobileNumber);
        return request;
    }
}
