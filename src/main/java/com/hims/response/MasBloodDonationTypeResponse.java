package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasBloodDonationTypeResponse {

    private Long donationTypeId;
    private String donationTypeCode;
    private String donationTypeName;
    private String description;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String LastUpdateBy;
    private String createdBy;
}
