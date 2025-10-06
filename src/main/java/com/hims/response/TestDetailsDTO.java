package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestDetailsDTO {
    private String testCode;
    private String testName;
    private String sampleName;
    private BigDecimal quantity;
    private String empanelledLab;
    private LocalDateTime dateTime;
    private String reason;
    private String remarks;
}
