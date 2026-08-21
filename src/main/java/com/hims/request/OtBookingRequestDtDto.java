package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtBookingRequestDtDto {

    private Long otBookingRequestDtId;
    private Long surgeryTypeId;
    private Long surgeryId;
    private Long sequenceNo;
    private Integer expectedDurationMin;
    private String status;
    private String lastChgBy;
}