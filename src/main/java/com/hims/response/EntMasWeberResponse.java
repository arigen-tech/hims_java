package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class EntMasWeberResponse {

    private Long id;
    private String weberResult;
    private String status;
    private LocalDateTime lastUpdateDate;
}
