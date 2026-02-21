package com.hims.request;

import lombok.Data;

@Data
public class DonorRegistrationRequest {
    BloodDonorPersonalDetailsRequest bloodDonorPersonalDetailsRequest;
    BloodDonorScreeningRequest bloodDonorScreeningRequest;
}
