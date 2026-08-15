package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.exception.BusinessException;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.DocumentCreateRequest;
import com.seaman.model.request.DocumentRequestValidateRequest;
import com.seaman.model.request.DocumentUpdateRequest;
import com.seaman.model.response.DocumentCreateResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.model.response.DocumentRequestValidateResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.service.DocumentService;
import com.seaman.service.DocumentRequestItemFileService;
import com.seaman.service.MessageCodeService;
import com.seaman.utils.ObjectValidatorUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Base64;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Documents", description = "จัดการใบรับรอง (Certificate) และเอกสารของลูกเรือ")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class DocumentController extends BaseController {

    private final MessageCodeService messageCodeService;
    private final DocumentService documentService;
    private final DocumentRequestItemFileService documentRequestItemFileService;

    @Operation(summary = "รายการ COT", description = "ดึงรายการ Certificate of Training แบบแบ่งหน้า")
    @GetMapping(Routes.DOCUMENTS_LIST_COT)
    public ResponseEntity<SuccessResponse<PageDocumentResponse>> documentListCot(HttpServletRequest httpServletRequest,
            @Parameter(description = "ตำแหน่งเริ่มต้น (0-based)", required = true) @RequestParam("offSet") int offSet) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.pageDocument(offSet, "COT")
        ).build());
    }

    @Operation(summary = "รายการ Document", description = "ดึงรายการเอกสารประเภท Document แบบแบ่งหน้า")
    @GetMapping(Routes.DOCUMENTS_LIST_DOC)
    public ResponseEntity<SuccessResponse<PageDocumentResponse>> documentListDoc(HttpServletRequest httpServletRequest,
            @Parameter(description = "ตำแหน่งเริ่มต้น (0-based)", required = true) @RequestParam("offSet") int offSet) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.pageDocument(offSet, "Document")
        ).build());
    }

    @Operation(summary = "เอกสารใกล้หมดอายุ", description = "รายการใบรับรองที่จะหมดอายุภายใน 18 เดือน")
    @GetMapping(Routes.DOCUMENTS_LIST_CLOSE_TO_EXPIRATION)
    public ResponseEntity<SuccessResponse<PageDocumentResponse>> closeToExpiration(HttpServletRequest httpServletRequest,
            @Parameter(description = "ตำแหน่งเริ่มต้น (0-based)", required = true) @RequestParam("offSet") int offSet) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.closeToExpiration(offSet)
        ).build());
    }

    @Operation(summary = "สร้างใบรับรอง", description = "อัพโหลดและบันทึกใบรับรองใหม่ (รองรับ PNG, JPEG, PDF ในรูปแบบ Base64)")
    @PostMapping(Routes.CREATE_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentCreate(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentCreateRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
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

    @Operation(summary = "แก้ไขใบรับรอง", description = "อัพเดตข้อมูลใบรับรองที่มีอยู่")
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

    @Operation(summary = "ลบใบรับรอง", description = "ลบใบรับรองตาม document code (soft delete)")
    @DeleteMapping(Routes.DELETE_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentDelete(
            HttpServletRequest httpServletRequest,
            @Parameter(description = "รหัสเอกสาร", required = true) @RequestParam("certCode") String certCode) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.documentDelete(certCode)
        ).build());
    }

    @Operation(summary = "ข้อมูลใบรับรองสำหรับแก้ไข", description = "ดึงข้อมูลใบรับรองเพื่อนำไปแสดงในหน้าแก้ไข")
    @GetMapping(Routes.EDIT_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentEdit(
            HttpServletRequest httpServletRequest,
            @Parameter(description = "รหัสเอกสาร", required = true) @RequestParam("certCode") String certCode) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.documentEdit(certCode)
        ).build());
    }

    @Operation(summary = "ตรวจสอบเอกสารที่ขาด", description = "ตรวจสอบรายการเอกสารที่ยังไม่ครบหรือถูก reject สำหรับ document code ที่ระบุ")
    @PostMapping({Routes.DOCUMENT_RENEWALS_REQUEST_VALIDATE_AND_CREATE})
    public ResponseEntity<SuccessResponse<DocumentRequestValidateResponse>> validateDocumentItems(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentRequestValidateRequest request) {

        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.validateAndCreateDocumentRenewalsItems(request)
        ).build());
    }

    @Operation(summary = "Upload supporting document file",
            description = "รองรับ ID_CARD MAIN/FRONT/BACK, PASSPORT MAIN และ GENERAL MAIN")
    @PostMapping(value = Routes.DOCUMENT_REQUEST_ITEM_FILES,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<DocumentRequestItemUploadResponse>> uploadRequestItemFile(
            HttpServletRequest httpServletRequest,
            @PathVariable String itemCode,
            @RequestParam(defaultValue = "GENERAL") String documentType,
            @RequestParam String slotCode,
            @RequestPart("file") MultipartFile file) {
        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.<DocumentRequestItemUploadResponse>builder(
                AppStatus.SUCCESS_CODE, description,
                documentRequestItemFileService.upload(itemCode, documentType, slotCode, file)).build());
    }

    @Operation(summary = "ดูไฟล์ใบรับรอง", description = "ดาวน์โหลดไฟล์ใบรับรอง (รองรับ PNG, JPEG, PDF)")
    @GetMapping(Routes.VIEW_CERT)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getImage(
            @Parameter(description = "รหัสเอกสาร", required = true) @RequestParam("certCode") String certCode)
            throws MagicMatchNotFoundException, MagicException, MagicParseException {

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
