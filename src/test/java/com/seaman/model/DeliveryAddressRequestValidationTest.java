package com.seaman.model;

import com.seaman.model.request.DeliveryAddressRequest;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryAddressRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsCompleteAddressCodesAndPostalCode() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    void rejectsMissingFieldsAndInvalidPostalCode() {
        DeliveryAddressRequest request = valid();
        request.setFirstName(" ");
        request.setPostalCode("3917");
        assertFalse(validator.validate(request).isEmpty());
    }

    private DeliveryAddressRequest valid() {
        DeliveryAddressRequest request = new DeliveryAddressRequest();
        request.setFirstName("Somchai");
        request.setLastName("Sailor");
        request.setAddressLine("1 Main Road");
        request.setProvince("39");
        request.setDistrict("3902");
        request.setSubDistrict("390202");
        request.setPostalCode("39170");
        request.setIsDefault(true);
        return request;
    }
}
