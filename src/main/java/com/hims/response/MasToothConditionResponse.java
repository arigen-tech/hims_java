package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasToothConditionResponse {

    private Long conditionId;
    private String conditionName;
    private String isExclusive;
    private Integer points;
    private String status;
    private LocalDateTime lastUpdateDate;
}
