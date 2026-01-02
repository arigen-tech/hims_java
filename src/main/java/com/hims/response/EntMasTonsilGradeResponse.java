package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class EntMasTonsilGradeResponse {

    private Long id;
    private String tonsilGrade;
    private String status;
    private LocalDateTime lastUpdateDate;
}
