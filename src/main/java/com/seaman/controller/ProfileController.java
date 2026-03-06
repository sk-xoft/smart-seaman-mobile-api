package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.ProfileRequest;
import com.seaman.model.request.ProfileUserActivateRequest;
import com.seaman.model.response.ProfileResponse;
import com.seaman.model.response.RegisterResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.ProfileService;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Base64;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class ProfileController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final ProfileService profileService;

    @GetMapping(Routes.PROFILE)
    public ResponseEntity<SuccessResponse<ProfileResponse>> profile(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                profileService.getProfile()
        ).build());
    }

    @PostMapping(Routes.PROFILE_UPDATE)
    public ResponseEntity<SuccessResponse<RegisterResponse>> profileUpdate(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody ProfileRequest request) throws MagicMatchNotFoundException, MagicException, MagicParseException {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                profileService.profileUpdate(request)
        ).build());
    }

    @GetMapping(Routes.PROFILE_IMAGE)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getImage() throws MagicMatchNotFoundException, MagicException, MagicParseException {

        String fileBase64 = profileService.getProfileImage();

//        // 1. download img your location...
//        byte[] content = Base64.getDecoder().decode(fileBase64);
//
//        MagicMatch match = Magic.getMagicMatch(content);
//        String mimeType = match.getMimeType();
//        HttpHeaders headers = new HttpHeaders();
//
//        if("image/png".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_PNG);
//        }
//
//        if("image/jpeg".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_JPEG);
//        }
//
//        if("application/pdf".equals(mimeType)) {
//            headers.setContentType(MediaType.APPLICATION_PDF);
//        }
//
//        headers.setContentLength(content.length);
//
//        return new HttpEntity<byte[]>(content, headers);

        byte[] imageBytes = Base64.getDecoder().decode(fileBase64);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @DeleteMapping(Routes.PROFILE_INACTIVE)
    public ResponseEntity<SuccessResponse<RegisterResponse>> profileInactive(
            HttpServletRequest httpServletRequest
            ) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                profileService.profileInactive()
        ).build());
    }

    @PostMapping(Routes.PROFILE_ACTIVE)
    public ResponseEntity<SuccessResponse<RegisterResponse>> profileActive(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody ProfileUserActivateRequest request
    ) {
        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                profileService.profileActive(request)
        ).build());
    }

}
