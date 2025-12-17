package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasDietScheduleStatusResponse {
    private Long dietScheduleStatusId;
    private String statusName;
    private String description;
    private String status;
    private LocalDateTime lastUpdateDate;
//    private String createdBy;
//    private String lastUpdatedBy;

}
