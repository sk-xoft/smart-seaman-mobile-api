package com.seaman.controller;

import com.seaman.constant.Routes;
import com.seaman.service.OmiseWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OmiseWebhookController extends BaseController {
    private final OmiseWebhookService webhookService;

    @PostMapping(Routes.OMISE_WEBHOOK)
    public ResponseEntity<Void> omise(
            @RequestBody String rawBody,
            @RequestHeader(value = "Omise-Signature", required = false) String signature,
            @RequestHeader(value = "Omise-Signature-Timestamp", required = false) String timestamp) {
        webhookService.handle(rawBody, signature, timestamp);
        return ResponseEntity.ok().build();
    }
}
