package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class GynMasMenstrualPatternResponse {

    private Long id;
    private String patternValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
