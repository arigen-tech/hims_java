package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasDietPreferenceResponse {
    private Long dietPreferenceId;
    private String preferenceName;
    private String description;
    private String status;
    private LocalDateTime lastUpdateDate;
//    private String createdBy;
//    private String lastUpdatedBy;
}
