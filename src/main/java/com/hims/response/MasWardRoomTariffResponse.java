package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class MasWardRoomTariffResponse {

    private Long id;
    private Long wardId;
    private String wardName;
    private Long roomId;
    private String roomName;
    private BigDecimal tariff;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedDate;
}