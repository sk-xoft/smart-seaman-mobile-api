package com.seaman.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppActivateControllerTest {

    @Test
    void androidAssetLinksReturnsDevJsonWhenProfileIsNotProd() throws Exception {
        AppActivateController controller = new AppActivateController();
        ReflectionTestUtils.setField(controller, "activeProfile", "dev");

        ResponseEntity<String> response = controller.getRadarData();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("relation"));
    }

    @Test
    void appleAppSiteAssociationReturnsDevJsonWhenProfileIsNotProd() throws Exception {
        AppActivateController controller = new AppActivateController();
        ReflectionTestUtils.setField(controller, "activeProfile", "dev");

        ResponseEntity<String> response = controller.getRenderDataIos();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("applinks"));
    }
}
