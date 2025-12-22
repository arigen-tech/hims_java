package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpthMasDistanceVisionResponse {

    private Long id;
    private String visionValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
