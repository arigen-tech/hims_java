package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSpecialtyCenterRequest {
    private String specialtyCenterName;
    private Long centerId;
    private Boolean isPrimary;
}
