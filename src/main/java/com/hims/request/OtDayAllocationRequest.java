package com.hims.request;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtDayAllocationRequest {

    private Long otId;

    private Long departmentId;

    private String dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;


}