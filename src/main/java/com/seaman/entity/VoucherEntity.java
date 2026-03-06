package com.seaman.entity;

import lombok.Data;

@Data
public class VoucherEntity extends CommonEntity {

    private String voucherId;
    private String voucherTitle;
    private String voucherPicture;
    private String voucherDetails;
    private String voucherTotal;
    private String voucherRemaining;
    private String voucherStartDate;
    private String voucherEndDate;
    private String voucherBarCode;
    private String voucherQrcode;
    private String voucherSmartSeamanId;
    private String voucherStatus;

}
