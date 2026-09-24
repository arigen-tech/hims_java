package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloodIssueResponse {

    private Long requestHdId;
    private Long requestDtId;
    private String requestNo;
    private String inpatientNo;
    private String patientName;
    private String bloodGroup;
    private String component;
    private Integer unitsReserved;
    private String requestDept;
    private String urgency;
    private String requiredBy;
    private String reservedOn;
}
