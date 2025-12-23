package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ObMasStationPresentingRequest {

    @NotBlank
    private String stationValue;
}
