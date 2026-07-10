package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasIpdBillingTypeResponse {
    private Long billingTypeId;
    private String billingTypeName;
    private String description;
    private String status;

    private LocalDateTime lastUpdateDate;


}
