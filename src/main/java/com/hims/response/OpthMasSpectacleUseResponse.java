package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpthMasSpectacleUseResponse {

    private Long id;
    private String useName;
    private String status;
    private LocalDateTime lastUpdateDate;
}
