package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter @Setter
public class DeliveryEntity {
    private String id;
    private String requestId;
    private String deliveryAddressId;
    private String trackingNo;
    private String carrier;
    private LocalDate shippedDate;
    private String deliveryStatus;
    private Date shippedRecordedAt;
    private String shippedBy;
    private Date deliveredAt;
    private Date createdAt;
    private Date updatedAt;
}
