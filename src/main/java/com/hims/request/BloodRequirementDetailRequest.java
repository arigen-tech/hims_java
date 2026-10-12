package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BloodRequirementDetailRequest {
    private Long componentId;
    private Integer unitsRequired;
    private String urgency;
    private LocalDateTime requiredDateTime;
    private String indication;
    private String remarks;
}
