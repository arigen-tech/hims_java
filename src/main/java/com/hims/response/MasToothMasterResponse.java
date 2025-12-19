package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasToothMasterResponse {

    private Long toothId;
    private String toothNumber;
    private String toothType;
    private Integer quadrant;
    private Integer displayOrder;
    private String status;
    private LocalDateTime lastUpdateDate;

}