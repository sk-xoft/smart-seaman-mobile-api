package com.seaman.model.response;

import lombok.Data;

@Data
public class VoucherModel {
    private String voucherId;
    private String voucherTitle;
    private String voucherDetails;
    private String voucherStartDate;
    private String voucherEndDate;
}
