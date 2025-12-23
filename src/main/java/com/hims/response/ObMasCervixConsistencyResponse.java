package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ObMasCervixConsistencyResponse {

    private Long id;
    private String cervixConsistency;
    private String status;
    private LocalDateTime lastUpdateDate;
}
