package com.seaman.model.response;

import lombok.Data;
import java.util.List;

@Data
public class BannerResponse {
    private List<BannerModel> banners;
}
