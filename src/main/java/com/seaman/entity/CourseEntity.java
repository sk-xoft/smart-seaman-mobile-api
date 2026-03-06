package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CourseEntity {
    private String courseId;
    private String courseCode;
    private String courseNameEn;
    private String courseNameTh;
    private String courseDescription;
    private String courseType;
}
