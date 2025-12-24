package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class EntMasRinneResponse {

    private Long id;
    private String rinneResult;
    private String status;
    private LocalDateTime lastUpdateDate;
}
