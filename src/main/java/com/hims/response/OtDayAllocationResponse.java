package com.hims.response;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtDayAllocationResponse {

    private Long otDayAllocationId;

    private Long otId;
    private String otCode;
    private String otName;

    private Long departmentId;
    private String departmentName;

    private String dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    private String status;

    private Long lastChgBy;
    private LocalDateTime lastChgDate;
}