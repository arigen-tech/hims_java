package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class EntMasMucosaResponse {

    private Long id;
    private String mucosaStatus;
    private String status;
    private LocalDateTime lastUpdateDate;
}
