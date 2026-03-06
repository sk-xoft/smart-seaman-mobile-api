package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.exception.BusinessException;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.DocumentCreateRequest;
import com.seaman.model.request.DocumentUpdateRequest;
import com.seaman.model.response.DocumentCreateResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.service.DocumentService;
import com.seaman.service.MessageCodeService;
import com.seaman.utils.ObjectValidatorUtils;
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
public class DocumentController extends BaseController {

    private final MessageCodeService messageCodeService;
    private final DocumentService documentService;

    @GetMapping(Routes.DOCUMENTS_LIST_COT)
    public ResponseEntity<SuccessResponse<PageDocumentResponse>> documentListCot(HttpServletRequest httpServletRequest, @RequestParam("offSet") int offSet) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.pageDocument(offSet, "COT")
        ).build());

    }

    @GetMapping(Routes.DOCUMENTS_LIST_DOC)
    public ResponseEntity<SuccessResponse<PageDocumentResponse>> documentListDoc(HttpServletRequest httpServletRequest, @RequestParam("offSet") int offSet) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.pageDocument(offSet, "Document")
        ).build());

    }

    @GetMapping(Routes.DOCUMENTS_LIST_CLOSE_TO_EXPIRATION)
    public ResponseEntity<SuccessResponse<PageDocumentResponse>> closeToExpiration(HttpServletRequest httpServletRequest, @RequestParam("offSet") int offSet) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.closeToExpiration(offSet)
        ).build());
    }

    @PostMapping(Routes.CREATE_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentCreate(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentCreateRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        // Validate body request
        // This validation format
        if(!ObjectValidatorUtils.verifyDateFormat(request.getCertStartDate())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, request.getCertStartDate());
        }

        if("9999-99-99".equals(request.getCertEndDate())) {
            // this case is expire.
            request.setCertEndDate(null);
        } else {

            if (!ObjectValidatorUtils.verifyDateFormat(request.getCertEndDate())) {
                throw new BusinessException(AppStatus.INVALID_FORMAT, request.getCertEndDate());
            }
        }

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.documentCreate(request)
        ).build());
    }

    @PostMapping(Routes.UPDATE_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentUpdate(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentUpdateRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        // Validate body request
        // This validation format
        if(!ObjectValidatorUtils.verifyDateFormat(request.getCertStartDate())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, request.getCertStartDate());
        }

        if("9999-99-99".equals(request.getCertEndDate())) {
            // this case is expire.
            request.setCertEndDate(null);
        } else {

            if (!ObjectValidatorUtils.verifyDateFormat(request.getCertEndDate())) {
                throw new BusinessException(AppStatus.INVALID_FORMAT, request.getCertEndDate());
            }
        }

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.documentUpdate(request)
        ).build());
    }

    @DeleteMapping(Routes.DELETE_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentDelete(
            HttpServletRequest httpServletRequest, @RequestParam("certCode") String certCode) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.documentDelete(certCode)
        ).build());
    }

    @GetMapping(Routes.EDIT_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentEdit(
            HttpServletRequest httpServletRequest, @RequestParam("certCode") String certCode) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.documentEdit(certCode)
        ).build());
    }

    @GetMapping(Routes.VIEW_CERT)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getImage(@RequestParam("certCode") String certCode) throws MagicMatchNotFoundException, MagicException, MagicParseException {

        String fileBase64 = documentService.viewCert(certCode);

        // 1. download img your location...
        byte[] content = Base64.getDecoder().decode(fileBase64);

        MagicMatch match = Magic.getMagicMatch(content);
        String mimeType = match.getMimeType();
        HttpHeaders headers = new HttpHeaders();

        if("image/png".equals(mimeType)) {
            headers.setContentType(MediaType.IMAGE_PNG);
        }

        if("image/jpeg".equals(mimeType)) {
            headers.setContentType(MediaType.IMAGE_JPEG);
        }

        if("application/pdf".equals(mimeType)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
        }

        headers.setContentLength(content.length);

        return new HttpEntity<byte[]>(content, headers);
    }
}
