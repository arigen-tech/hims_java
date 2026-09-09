package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BloodRequestRequest {

    private Long inpatientId;
    private Long wardId;
    private Long patientId;
    private Long requestDepartment;
    private Long bloodGroupId;
    private List<BloodRequirementDetailRequest> bloodRequirementDetails;
}
