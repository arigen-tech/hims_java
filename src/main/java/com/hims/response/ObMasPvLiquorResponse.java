package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ObMasPvLiquorResponse {

    private Long id;
    private String liquorValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
