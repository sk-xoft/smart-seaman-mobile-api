package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class ListSchoolTrainingEntity implements Comparable<ListSchoolTrainingEntity> {
    private String companyCode;
    private String companyNameEn;
    private String companyNameTh;
    private String companyColour;
    private String courseColour;
    private String courseId;
    private String courseCode;
    private String courseNameEn;
    private String courseNameTh;
    private String courseType;
    private String courseOnlineDate;
    private String courseOnsiteDate;
    private String courseTotalDays;
    private String coursePrice;

    private String companyLine;
    private String companyFacebook;
    private String companyPhone1;

    private String courseStartDate;
    private String courseEndDate;

    private String dateForCheck;

    @Override
    public int compareTo(@NotNull ListSchoolTrainingEntity o) {
        int var1 = Integer.parseInt(this.dateForCheck);
        int var2 = Integer.parseInt(o.getDateForCheck());
        return var1 - var2;
    }
}
