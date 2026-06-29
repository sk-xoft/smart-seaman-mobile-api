package com.seaman.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.firewall.RequestRejectedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityConfigurationTest {

    @Test
    void requestRejectedHandlerReturnsBadRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1//profile");
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityConfiguration.requestRejectedHandler()
                .handle(request, response, new RequestRejectedException("The request was rejected"));

        assertEquals(400, response.getStatus());
    }
}
