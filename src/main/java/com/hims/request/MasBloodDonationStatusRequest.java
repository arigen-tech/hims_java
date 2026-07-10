package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodDonationStatusRequest {

    private String donationStatusCode;
    private String donationStatusName;
    private String description;
    private String isFinal;
}
