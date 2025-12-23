package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class GynMasMenarcheAgeResponse {

    private Long id;
    private String menarcheAge;
    private String status;
    private LocalDateTime lastUpdateDate;
}
