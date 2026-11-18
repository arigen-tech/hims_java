package com.hims.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodComponentExpiryResponse {

    private Long inventoryId;
    private String unitNo;
    private String component;
    private String bloodGroup;
    private LocalDate expiryDate;
    private Long daysLeft;
    private String inventoryStatus;
}