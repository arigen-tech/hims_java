package com.hims.request;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class DonorRegistrationRequest {

    BloodDonorPersonalDetailsRequest bloodDonorPersonalDetailsRequest;
    @Valid
    BloodDonorScreeningRequest bloodDonorScreeningRequest;
}
