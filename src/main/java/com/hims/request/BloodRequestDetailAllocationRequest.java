package com.hims.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BloodRequestDetailAllocationRequest {

    @NotNull
    private Long requestDtId;
    @NotEmpty
    private List<Long> inventoryIds;
}
