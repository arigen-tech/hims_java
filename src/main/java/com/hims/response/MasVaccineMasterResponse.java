package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class MasVaccineMasterResponse {

    private Long vaccineId;
    private String vaccineLabel;
    private String recommendedAge;
    private String vaccineGroup;
    private Integer displayOrder;
    private String isMultiDose;
    private Integer dosePerVial;
    private String status;
    private LocalDateTime lastUpdateDate;
}
