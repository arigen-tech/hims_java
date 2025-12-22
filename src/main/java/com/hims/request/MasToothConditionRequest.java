package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasToothConditionRequest {

    private String conditionName;
    private String isExclusive;
    private Integer points;
}
