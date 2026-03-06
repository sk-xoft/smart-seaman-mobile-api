package com.seaman.model.response;

import lombok.Data;

@Data
public class NewsModel {
    private String id;
    private String title;
    private String body;
    private String newsDate;
}
