package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class GynMasPapSmearResponse {

    private Long id;
    private String papResult;
    private String status;
    private LocalDateTime lastUpdateDate;
}
