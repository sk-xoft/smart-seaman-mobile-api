package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class DocumentRequestItemEntity {

    private String id;
    private String mobileUserUuid;
    private String documentName;
    private Integer sortOrder;
    private Integer fileUploaded;
    private String filePath;
    private Date fileUploadedAt;
    private String checkResult;
    private String checkNote;
    private Integer isUpdated;
    private Date checkedAt;
    private String checkedBy;
    private Date createdAt;
    private Date updatedAt;

    // from JOIN with m_document_setting_requires
    private String documentCode;
    private Integer isRequired;
}
