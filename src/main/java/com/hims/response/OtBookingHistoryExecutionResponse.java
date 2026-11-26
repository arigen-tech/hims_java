package com.hims.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OtBookingHistoryExecutionResponse extends OtBookingHistoryResponse {
    private String executionStatus;
}
