package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class EntMasSeptumResponse {

    private Long id;
    private String septumStatus;
    private String status;
    private LocalDateTime lastUpdateDate;
}
