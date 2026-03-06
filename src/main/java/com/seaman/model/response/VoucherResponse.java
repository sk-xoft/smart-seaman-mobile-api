package com.seaman.model.response;

import lombok.Data;
import java.util.List;

@Data
public class VoucherResponse {
    private List<VoucherModel> voucherModel;
}
