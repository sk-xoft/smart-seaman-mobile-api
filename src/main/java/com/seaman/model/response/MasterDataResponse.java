package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class MasterDataResponse {
    private List<CompanyResponse> company;
    private List<PositionResponse> position;
}
