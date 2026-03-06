package com.seaman.model.response;

import com.seaman.entity.ListSchoolTrainingEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Data
public class SchoolTrainingResponse {
    private List<ListSchoolTrainingEntity> schoolTrainings;
}
