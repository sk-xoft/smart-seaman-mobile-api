package com.seaman.model.response;

import com.seaman.entity.DocumentEntity;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class PageDocumentResponse {
    private int itemTotal;
    private boolean isLast;
    private List<DocumentEntity> items;
}
