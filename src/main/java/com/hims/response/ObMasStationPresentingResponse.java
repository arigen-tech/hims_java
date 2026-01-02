package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class ObMasStationPresentingResponse {

    private Long id;
    private String stationValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
