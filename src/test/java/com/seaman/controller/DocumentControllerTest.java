package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.DocumentCreateRequest;
import com.seaman.model.request.DocumentUpdateRequest;
import com.seaman.model.response.DocumentCreateResponse;
import com.seaman.model.response.DocumentRequestItemPreviewResponse;
import com.seaman.model.response.DocumentRequestItemResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.model.response.DocumentUpdateResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.service.DocumentRequestItemFileService;
import com.seaman.service.DocumentService;
import com.seaman.service.MessageCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerTest {
    private static final String PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADElEQVR42mP8z8AARQAFAAH+Af9qAAAAAElFTkSuQmCC";

    private HttpServletRequest request;
    private DocumentService documents;
    private DocumentRequestItemFileService itemFileService;
    private DocumentController controller;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        documents = mock(DocumentService.class);
        itemFileService = mock(DocumentRequestItemFileService.class);
        when(request.getAttribute(AppSys.LANGUAGE)).thenReturn("TH");
        when(messages.getMessageDescription(AppStatus.SUCCESS_CODE, "TH")).thenReturn("success");
        controller = new DocumentController(messages, documents, itemFileService);
    }

    @Test
    void listEndpointsDelegateWithCorrectDocumentTypes() {
        PageDocumentResponse page = new PageDocumentResponse();
        when(documents.pageDocument(0, "COT")).thenReturn(page);
        when(documents.pageDocument(1, "Document")).thenReturn(page);
        when(documents.closeToExpiration(2)).thenReturn(page);

        assertSame(page, controller.documentListCot(request, 0).getBody().getData());
        assertSame(page, controller.documentListDoc(request, 1).getBody().getData());
        assertSame(page, controller.closeToExpiration(request, 2).getBody().getData());
        verify(documents).pageDocument(0, "COT");
        verify(documents).pageDocument(1, "Document");
        verify(documents).closeToExpiration(2);
    }

    @Test
    void createUpdateDeleteAndEditDelegateToService() {
        DocumentCreateResponse data = new DocumentCreateResponse();
        DocumentUpdateResponse updateData = new DocumentUpdateResponse();
        DocumentCreateRequest create = new DocumentCreateRequest();
        create.setCertStartDate("2026-07-26");
        create.setCertEndDate("9999-99-99");
        DocumentUpdateRequest update = new DocumentUpdateRequest();
        update.setCertStartDate("2026-07-26");
        update.setCertEndDate("2027-07-26");
        when(documents.documentCreate(create)).thenReturn(data);
        when(documents.documentUpdate(update)).thenReturn(updateData);
        when(documents.documentDelete("CERT001")).thenReturn("success");
        when(documents.documentEdit("CERT001")).thenReturn(data);

        assertSame(data, controller.documentCreate(request, create).getBody().getData());
        assertNull(create.getCertEndDate());
        assertSame(updateData,
                ((SuccessResponse<?>) controller.documentUpdate(request, update).getBody()).getData());
        assertSame("success",
                ((SuccessResponse<?>) controller.documentDelete(request, "CERT001").getBody()).getData());
        assertSame(data, controller.documentEdit(request, "CERT001").getBody().getData());
    }

    @Test
    void viewCertReturnsDetectedImageContentType() throws Exception {
        when(documents.viewCert("CERT001")).thenReturn(PNG_BASE64);

        HttpEntity<byte[]> image = controller.getImage("CERT001");

        assertEquals(MediaType.IMAGE_PNG, image.getHeaders().getContentType());
        assertTrue(image.getBody().length > 0);
        verify(documents).viewCert("CERT001");
    }

    @Test
    void uploadRequestItemFileDelegatesToItemFileService() {
        DocumentRequestItemUploadResponse data = new DocumentRequestItemUploadResponse();
        MockMultipartFile file = new MockMultipartFile("file", "id-card.png", "image/png", new byte[]{1, 2, 3});
        when(itemFileService.upload("ITEM001", "ID_CARD", "SLOT001", file)).thenReturn(data);

        assertSame(data, controller.uploadRequestItemFile(request, "ITEM001", "ID_CARD", "SLOT001", file)
                .getBody().getData());
        verify(itemFileService).upload("ITEM001", "ID_CARD", "SLOT001", file);
    }

    @Test
    void listProfileItemsDelegatesToDocumentService() {
        java.util.List<DocumentRequestItemResponse> data =
                java.util.Collections.singletonList(new DocumentRequestItemResponse());
        when(documents.listProfileItems()).thenReturn(data);

        assertSame(data, controller.listProfileItems(request).getBody().getData());
        verify(documents).listProfileItems();
    }

    @Test
    void previewProfileItemDelegatesToItemFileService() {
        DocumentRequestItemPreviewResponse data = new DocumentRequestItemPreviewResponse();
        when(itemFileService.preview("MRI001")).thenReturn(data);

        assertSame(data, controller.previewProfileItem(request, "MRI001").getBody().getData());
        verify(itemFileService).preview("MRI001");
    }

    private <T> void assertSuccess(SuccessResponse<T> body, T data) {
        assertEquals(AppStatus.SUCCESS_CODE, body.getCode());
        assertEquals("success", body.getDescription());
        assertSame(data, body.getData());
    }
}
