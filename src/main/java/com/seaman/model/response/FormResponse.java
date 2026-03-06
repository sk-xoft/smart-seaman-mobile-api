package com.seaman.model.response;

import com.seaman.entity.FormEntity;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class FormResponse {
    List<FormEntity> forms;
}
