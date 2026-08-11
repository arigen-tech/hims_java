package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionHeaderApproveRequest {

    private Long prescriptionHeaderId;
    private List<PrescriptionDetailsApproveRequest> prescriptionDetails;
}
