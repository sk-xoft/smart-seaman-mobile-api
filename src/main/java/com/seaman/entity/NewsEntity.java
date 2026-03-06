package com.seaman.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Setter
@Getter
@Data
public class NewsEntity extends CommonEntity implements Serializable {
    private Integer newsId;
    private String newsTitle;
    private String newsPictureFileName;
    private String newsType;
    private String newsDetails;
    private String newsStatus;
}
