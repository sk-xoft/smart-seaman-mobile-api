package com.seaman.controller;

import com.seaman.constant.AppSys;
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

@RestController
@RequiredArgsConstructor
public class AppActivateController {


    @Value("${spring.profiles.active}")
    private String activeProfile;

    @GetMapping("/.well-known/assetlinks.json")
    public ResponseEntity<String> getRadarData() throws IOException {
        ClassPathResource staticDataResource = null;

        if(AppSys.PROFILE_PROD.equals(activeProfile)){
            staticDataResource =  new ClassPathResource("assetlinks.json");
        } else {
            staticDataResource =  new ClassPathResource("assetlinks-dev.json");
        }

        String staticDataString = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);

        final HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(
                staticDataString,
                httpHeaders,
                HttpStatus.OK
        );
    }

    @GetMapping("/.well-known/apple-app-site-association")
    public ResponseEntity<String> getRenderDataIos() throws IOException {

        ClassPathResource staticDataResource = null;

        if(AppSys.PROFILE_PROD.equals(activeProfile)) {
            staticDataResource =  new ClassPathResource("apple-app-site-association.json");
        } else {
            staticDataResource =  new ClassPathResource("apple-app-site-association-dev.json");
        }
        String staticDataString = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);

        final HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(
                staticDataString,
                httpHeaders,
                HttpStatus.OK
        );
    }

}
