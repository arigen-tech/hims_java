package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasPatientPreparationResponse {

    private Long preparationId;
    private String preparationCode;
    private String preparationName;
    private String instructions;
    private String applicableTo;
    private String status;
    private LocalDateTime lastUpdateDate;
}
