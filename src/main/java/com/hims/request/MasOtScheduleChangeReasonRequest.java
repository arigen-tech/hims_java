package com.hims.request;

import lombok.Data;

@Data
public class MasOtScheduleChangeReasonRequest {

    private String reason;
    private String applicableFor;
}
