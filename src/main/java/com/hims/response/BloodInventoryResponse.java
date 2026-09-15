package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BloodInventoryResponse {

    private Long inventoryId;
    private String unitNo;
    private Long bloodGroupId;
    private Integer volumeMl;
    private LocalDate expiryDate;
    private Long componentId;
    private String compatibility;
    private String status;
    private Boolean preferred;
}
