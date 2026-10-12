package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcedureSessionRequest {
    private Integer sessionNo;
    private LocalDateTime scheduledDateTime;
    private String remarks;
}
