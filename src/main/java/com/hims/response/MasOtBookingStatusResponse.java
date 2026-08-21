package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasOtBookingStatusResponse {

    private Long bookingStatusId;
    private String statusCode;
    private String statusName;
    private String description;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}