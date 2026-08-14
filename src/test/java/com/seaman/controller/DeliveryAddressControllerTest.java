package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.model.response.DeliveryAddressResponse;
import com.seaman.service.DeliveryAddressService;
import com.seaman.service.MessageCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.seaman.model.request.DeliveryAddressRequest;

class DeliveryAddressControllerTest {
    @Test
    void getDefaultWrapsServiceResultInSuccessResponse() {
        DeliveryAddressService service = mock(DeliveryAddressService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DeliveryAddressResponse data = new DeliveryAddressResponse();
        data.setId("address-id");
        when(service.getDefault()).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DeliveryAddressController controller = new DeliveryAddressController(service, messages);

        ResponseEntity<com.seaman.model.common.SuccessResponse<DeliveryAddressResponse>> response =
                controller.getDefault(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("address-id", response.getBody().getData().getId());
        verify(service).getDefault();
    }

    @Test
    void createAndUpdateDelegateValidatedPayloadToService() {
        DeliveryAddressService service = mock(DeliveryAddressService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DeliveryAddressRequest input = new DeliveryAddressRequest();
        DeliveryAddressResponse data = new DeliveryAddressResponse();
        data.setId("address-id");
        when(service.create(input)).thenReturn(data);
        when(service.createForRenewal("REQ001", input)).thenReturn(data);
        when(service.update("address-id", input)).thenReturn(data);
        DeliveryAddressController controller = new DeliveryAddressController(service, messages);

        assertEquals("address-id", controller.create(servletRequest, input).getBody().getData().getId());
        assertEquals("address-id", controller.createForRenewal(servletRequest, "REQ001", input)
                .getBody().getData().getId());
        assertEquals("address-id", controller.update(servletRequest, "address-id", input)
                .getBody().getData().getId());
        verify(service).create(input);
        verify(service).createForRenewal("REQ001", input);
        verify(service).update("address-id", input);
    }

    @Test
    void updateMappingUsesAddressIdPathVariable() throws Exception {
        Method update = DeliveryAddressController.class.getMethod(
                "update", HttpServletRequest.class, String.class, DeliveryAddressRequest.class);
        Method createForRenewal = DeliveryAddressController.class.getMethod(
                "createForRenewal", HttpServletRequest.class, String.class, DeliveryAddressRequest.class);

        assertEquals("/delivery-addresses/{addressId}", update.getAnnotation(PutMapping.class).value()[0]);
        assertEquals("/delivery-addresses/{requestNo}", createForRenewal.getAnnotation(PostMapping.class).value()[0]);
    }
}
