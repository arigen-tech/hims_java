package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodDonationTypeRequest {

    @NotBlank
    private String donationTypeCode;

    @NotBlank
    private String donationTypeName;

    private String description;
}
