package com.hims.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BloodRequestAllocationRequest {

    @NotEmpty
    private List<BloodRequestDetailAllocationRequest> details;

}
