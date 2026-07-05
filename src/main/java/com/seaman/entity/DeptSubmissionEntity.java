package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter @Setter
public class DeptSubmissionEntity {
    private String id;
    private String requestId;
    private LocalDate submittedToDeptDate;
    private String submittedBy;
    private LocalDate availableFromDate;
    private LocalDate receivedFromDeptDate;
    private Date recordedAt;
    private Date updatedAt;
}
