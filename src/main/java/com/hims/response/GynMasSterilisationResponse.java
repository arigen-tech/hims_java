package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class GynMasSterilisationResponse {

    private Long id;
    private String sterilisationType;
    private String status;
    private LocalDateTime lastUpdateDate;
}
