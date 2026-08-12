package com.seaman.service;

import com.seaman.entity.CompanyEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.PositionsEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentResponse;
import com.seaman.model.response.MasterDataDocumentResponse;
import com.seaman.model.response.MasterDataResponse;
import com.seaman.repository.CompanyRepository;
import com.seaman.repository.DocumentRepository;
import com.seaman.repository.PositionRepository;
import com.seaman.repository.ThailandAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Covers the MasterDataService methods not already exercised by
 * {@link MasterDataServiceAddressTest} (which covers only provinces/districts/subdistricts).
 */
@ExtendWith(MockitoExtension.class)
class MasterDataServiceTest {

    @Mock HttpServletRequest httpServletRequest;
    @Mock CompanyRepository companyRepository;
    @Mock PositionRepository positionRepository;
    @Mock DocumentRepository documentRepository;
    @Mock ThailandAddressRepository thailandAddressRepository;

    private MasterDataService service;

    @BeforeEach
    void setUp() {
        service = new MasterDataService(httpServletRequest, companyRepository, positionRepository,
                documentRepository, thailandAddressRepository);
    }

    // ---- list

    @Test
    void listUsesEnglishNamesWhenLanguageIsEn() {
        when(httpServletRequest.getHeader(anyHeader())).thenReturn("EN");
        when(companyRepository.findAll()).thenReturn(List.of(company()));
        when(positionRepository.findAll()).thenReturn(List.of(position()));

        MasterDataResponse response = service.list();

        assertEquals(1, response.getCompany().size());
        assertEquals("Company EN", response.getCompany().get(0).getCompanyName());
        assertEquals(1, response.getPosition().size());
        assertEquals("Captain EN", response.getPosition().get(0).getPositionName());
    }

    @Test
    void listUsesThaiNamesWhenLanguageNotEn() {
        when(httpServletRequest.getHeader(anyHeader())).thenReturn("TH");
        when(companyRepository.findAll()).thenReturn(List.of(company()));
        when(positionRepository.findAll()).thenReturn(List.of(position()));

        MasterDataResponse response = service.list();

        assertEquals("Company TH", response.getCompany().get(0).getCompanyName());
        assertEquals("Captain TH", response.getPosition().get(0).getPositionName());
    }

    @Test
    void listReturnsEmptyListsWhenNoDataFound() {
        when(companyRepository.findAll()).thenReturn(Collections.emptyList());
        when(positionRepository.findAll()).thenReturn(Collections.emptyList());

        MasterDataResponse response = service.list();

        assertTrue(response.getCompany().isEmpty());
        assertTrue(response.getPosition().isEmpty());
    }

    @Test
    void listRethrowsExceptionFromRepository() {
        when(companyRepository.findAll()).thenThrow(new BusinessException("MA99999", "boom"));

        assertThrows(BusinessException.class, () -> service.list());
    }

    // ---- masterDataDocuments

    @Test
    void masterDataDocumentsSplitsDocumentAndCotByType() {
        when(httpServletRequest.getHeader(anyHeader())).thenReturn("EN");
        when(documentRepository.findByType("Document")).thenReturn(List.of(document("DOC001")));
        when(documentRepository.findByType("COT")).thenReturn(List.of(document("COT001")));

        MasterDataDocumentResponse response = service.masterDataDocuments();

        assertEquals(1, response.getDocuments().size());
        assertEquals("DOC001", response.getDocuments().get(0).getDocumentCode());
        assertEquals(1, response.getCot().size());
        assertEquals("COT001", response.getCot().get(0).getDocumentCode());
        assertEquals("EN Name", response.getDocuments().get(0).getDocumentName());
    }

    @Test
    void masterDataDocumentsUsesThaiNameWhenLanguageNotEn() {
        when(httpServletRequest.getHeader(anyHeader())).thenReturn("TH");
        when(documentRepository.findByType("Document")).thenReturn(List.of(document("DOC001")));
        when(documentRepository.findByType("COT")).thenReturn(Collections.emptyList());

        MasterDataDocumentResponse response = service.masterDataDocuments();

        assertEquals("TH Name", response.getDocuments().get(0).getDocumentName());
    }

    @Test
    void masterDataDocumentsReturnsEmptyListsWhenNoneFound() {
        when(documentRepository.findByType("Document")).thenReturn(Collections.emptyList());
        when(documentRepository.findByType("COT")).thenReturn(Collections.emptyList());

        MasterDataDocumentResponse response = service.masterDataDocuments();

        assertTrue(response.getDocuments().isEmpty());
        assertTrue(response.getCot().isEmpty());
    }

    // ---- masterDataDocumentsFull

    @Test
    void masterDataDocumentsFullAlwaysUsesEnglishNameAndCombinesLists() {
        // Regardless of Accept-Language, masterDataDocumentsFull() always uses documentNameEn.
        when(documentRepository.findByType("Document")).thenReturn(List.of(document("DOC001")));
        when(documentRepository.findByType("COT")).thenReturn(List.of(document("COT001")));

        List<DocumentResponse> result = service.masterDataDocumentsFull();

        assertEquals(2, result.size());
        assertEquals("EN Name", result.get(0).getDocumentName());
        assertEquals("EN Name", result.get(1).getDocumentName());
    }

    @Test
    void masterDataDocumentsFullReturnsEmptyListWhenNoneFound() {
        when(documentRepository.findByType("Document")).thenReturn(Collections.emptyList());
        when(documentRepository.findByType("COT")).thenReturn(Collections.emptyList());

        assertTrue(service.masterDataDocumentsFull().isEmpty());
    }

    // ---- fixtures

    private String anyHeader() {
        return org.mockito.ArgumentMatchers.anyString();
    }

    private CompanyEntity company() {
        CompanyEntity entity = new CompanyEntity();
        entity.setCompanyCode("COMP001");
        entity.setCompanyNameEn("Company EN");
        entity.setCompanyNameTh("Company TH");
        return entity;
    }

    private PositionsEntity position() {
        PositionsEntity entity = new PositionsEntity();
        entity.setPositionCode("POS001");
        entity.setPositionNameEn("Captain EN");
        entity.setPositionNameTh("Captain TH");
        return entity;
    }

    private DocumentEntity document(String code) {
        DocumentEntity entity = new DocumentEntity();
        entity.setDocumentCode(code);
        entity.setDocumentNameEn("EN Name");
        entity.setDocumentNameTh("TH Name");
        return entity;
    }
}
