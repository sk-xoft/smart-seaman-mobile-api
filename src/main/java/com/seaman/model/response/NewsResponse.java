package com.seaman.model.response;

import lombok.Data;
import java.util.List;

@Data
public class NewsResponse {
    private List<NewsModel> newsGeneral;
    private List<NewsModel> newsShip;
}
