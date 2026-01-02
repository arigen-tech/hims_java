package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class EntMasEarCanalResponse {

    private Long id;
    private String earCanalCondition;
    private String status;
    private LocalDateTime lastUpdateDate;
}
