package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasBloodDonationStatusResponse {

    private Long donationStatusId;
    private String donationStatusCode;
    private String donationStatusName;
    private String description;
    private String isFinal;
    private String status;
    private LocalDateTime createdDate;
    private String createdBy;
}
