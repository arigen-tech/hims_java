package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreviousDietHistoryResponse {
    private Long inpatientId;
    private Long dietOrderId;
    private Long dietTypeId;
    private String dietTypeName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String specialInstruction;
    private String orderedBy;
    private String status;

}
