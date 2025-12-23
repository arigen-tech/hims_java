package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ObMasCervixPositionResponse {

    private Long id;
    private String cervixPosition;
    private String status;
    private LocalDateTime lastUpdateDate;
}
