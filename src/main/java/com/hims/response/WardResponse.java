package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WardResponse {
    private Long WardId;
    private String wardName;
    private Long availableBed;
}
