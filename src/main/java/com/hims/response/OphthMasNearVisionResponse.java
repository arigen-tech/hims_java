package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OphthMasNearVisionResponse {

    private Long id;
    private String nearValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
