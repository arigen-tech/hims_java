package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LabRadioUpdateResponse {
    private Long billingHdId;
    private List<Long> billingHeaderIds;
    private String message;
}
