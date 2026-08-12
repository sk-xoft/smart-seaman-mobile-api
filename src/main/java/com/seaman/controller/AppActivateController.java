package com.seaman.controller;

import com.seaman.constant.AppSys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Tag(name = "App Verification", description = "Android App Links และ iOS Universal Links สำหรับ app verification")
@RestController
@RequiredArgsConstructor
public class AppActivateController {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Operation(summary = "Android Asset Links", description = "ไฟล์ assetlinks.json สำหรับ Android App Links verification")
    @GetMapping("/.well-known/assetlinks.json")
    public ResponseEntity<String> getRadarData() throws IOException {
        ClassPathResource staticDataResource = null;

        if (AppSys.PROFILE_PROD.equals(activeProfile)) {
            staticDataResource = new ClassPathResource("assetlinks.json");
        } else {
            staticDataResource = new ClassPathResource("assetlinks-dev.json");
        }

        String staticDataString = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(
                staticDataString,
                httpHeaders,
                HttpStatus.OK);
    }

    @Operation(summary = "iOS Apple App Site Association", description = "ไฟล์ apple-app-site-association สำหรับ iOS Universal Links verification")
    @GetMapping("/.well-known/apple-app-site-association")
    public ResponseEntity<String> getRenderDataIos() throws IOException {

        ClassPathResource staticDataResource = null;

        if (AppSys.PROFILE_PROD.equals(activeProfile)) {
            staticDataResource = new ClassPathResource("apple-app-site-association.json");
        } else {
            staticDataResource = new ClassPathResource("apple-app-site-association-dev.json");
        }
        String staticDataString = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(
                staticDataString,
                httpHeaders,
                HttpStatus.OK);
    }

}
