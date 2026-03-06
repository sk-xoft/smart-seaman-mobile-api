package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class PageCertificationEntity {
    private int itemTotal;
    private boolean isLast;
    private List<DocumentEntity> items;
}