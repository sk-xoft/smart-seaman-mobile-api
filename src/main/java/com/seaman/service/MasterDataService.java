package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.CompanyEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.PositionsEntity;
import com.seaman.exception.CommonException;
import com.seaman.model.response.*;
import com.seaman.repository.CompanyRepository;
import com.seaman.repository.DocumentRepository;
import com.seaman.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final HttpServletRequest httpServletRequest;
    private final CompanyRepository companyRepository;
    private final PositionRepository positionRepository;
    private final DocumentRepository documentRepository;

    public MasterDataResponse list() {

        String lang = httpServletRequest.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE);

        MasterDataResponse response =  new MasterDataResponse();
        List<CompanyEntity> companyEntityList =  new ArrayList<>();
        List<PositionsEntity> positionsEntityList = new ArrayList<>();

        try {

            companyEntityList =  companyRepository.findAll();
            positionsEntityList = positionRepository.findAll();

            // Company
            List<CompanyResponse> companyResponseList = new ArrayList<>();
            for(CompanyEntity item :companyEntityList){
                CompanyResponse companyResponse =  new CompanyResponse();
                companyResponse.setCompanyCode(item.getCompanyCode());
                companyResponse.setCompanyName(AppSys.LANG_EN.equals(lang) ? item.getCompanyNameEn() : item.getCompanyNameTh());
                companyResponseList.add(companyResponse);
            }
            response.setCompany(companyResponseList);

            // Position
            List<PositionResponse> positionResponseList = new ArrayList<>();
            for(PositionsEntity item : positionsEntityList) {
                PositionResponse positionResponse  = new PositionResponse();
                positionResponse.setPositionCode(item.getPositionCode());
                positionResponse.setPositionName(AppSys.LANG_EN.equals(lang) ? item.getPositionNameEn() : item.getPositionNameTh());
                positionResponseList.add(positionResponse);
            }
            response.setPosition(positionResponseList);
            log.info("Get master data success.");
        } catch (CommonException ce){
            log.error("{}",  ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw  ex;
        }

        return response;
    }

    public MasterDataDocumentResponse masterDataDocuments() {

        String lang = httpServletRequest.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE);

        MasterDataDocumentResponse response =  new MasterDataDocumentResponse();
        List<DocumentResponse> documentEntities =  new ArrayList<>();
        List<DocumentResponse> documentCOT =  new ArrayList<>();
        try {

            for(DocumentEntity item : documentRepository.findByType("Document")) {
                DocumentResponse documentResponse  = new DocumentResponse();
                documentResponse.setDocumentCode(item.getDocumentCode());
                documentResponse.setDocumentName(AppSys.LANG_EN.equals(lang) ? item.getDocumentNameEn(): item.getDocumentNameTh());
                documentResponse.setDocumentNameTh(item.getDocumentNameTh());
                documentEntities.add(documentResponse);
            }
            response.setDocuments(documentEntities);

            for(DocumentEntity item : documentRepository.findByType("COT")) {
                DocumentResponse documentResponse  = new DocumentResponse();
                documentResponse.setDocumentCode(item.getDocumentCode());
                documentResponse.setDocumentName(AppSys.LANG_EN.equals(lang) ? item.getDocumentNameEn(): item.getDocumentNameTh());
                documentResponse.setDocumentNameTh(item.getDocumentNameTh());
                documentCOT.add(documentResponse);
            }
            response.setCot(documentCOT);

            log.info("Get master data documents is success.");
        } catch (CommonException ce){
            log.error("{}",  ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("{} error -> {}", "Master Data Document", ex);
            throw  ex;
        }

        return response;
    }

    public List<DocumentResponse>  masterDataDocumentsFull() {

        String lang = httpServletRequest.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE);
        List<DocumentResponse> listFull = new ArrayList<>();

        List<DocumentResponse> documentEntities =  new ArrayList<>();
        List<DocumentResponse> documentCOT =  new ArrayList<>();

        try {

            for(DocumentEntity item : documentRepository.findByType("Document")) {
                DocumentResponse documentResponse  = new DocumentResponse();
                documentResponse.setDocumentCode(item.getDocumentCode());
                documentResponse.setDocumentName(item.getDocumentNameEn());
                documentResponse.setDocumentNameTh(item.getDocumentNameTh());
                documentEntities.add(documentResponse);
            }

            for(DocumentEntity item : documentRepository.findByType("COT")) {
                DocumentResponse documentResponse  = new DocumentResponse();
                documentResponse.setDocumentCode(item.getDocumentCode());
                documentResponse.setDocumentName(item.getDocumentNameEn());
                documentResponse.setDocumentNameTh(item.getDocumentNameTh());
                documentCOT.add(documentResponse);
            }

            listFull.addAll(documentEntities);
            listFull.addAll(documentCOT);
            log.info("Get master data documents is success.");

        } catch (CommonException ce){
            log.error("{}",  ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw  ex;
        }

        return listFull;
    }

}
