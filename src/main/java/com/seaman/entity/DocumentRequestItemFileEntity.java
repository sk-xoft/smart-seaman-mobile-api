package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class DocumentRequestItemFileEntity {
    private String id;
    private String profileRequestItemId;
    private String requestItemId;
    private String documentMasterRequestItemCode;
    private String documentType;
    private String slotCode;
    private String storageKey;
    private String originalFileName;
    private String mimeType;
    private Long fileSize;
    private Integer fileUploaded;
    private Date fileUploadedAt;
    private String checkResult;
    private String checkNote;
    private Boolean isUpdated;
}
