package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpthMasColorVisionResponse {

    private Long id;
    private String colorValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
