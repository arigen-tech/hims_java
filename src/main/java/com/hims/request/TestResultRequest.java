package com.hims.request;

import lombok.Data;

import java.time.LocalDate;
@Data
public class TestResultRequest {
    private Long testId;
    private String result;
    private LocalDate testDate;
    private String remarks;
}
