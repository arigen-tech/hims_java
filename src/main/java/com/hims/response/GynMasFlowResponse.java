package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class GynMasFlowResponse {

    private Long id;
    private String flowValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
