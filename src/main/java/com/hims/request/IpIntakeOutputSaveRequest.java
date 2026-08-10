package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
@Data
public class IpIntakeOutputSaveRequest {
    @NotNull(message = "Inpatient ID is required")
    private Long inpatientId;

    @Valid
    @NotEmpty(message = "At least one intake/output entry is required")
    private List<IpIntakeOutputEntryRequest> entries;
}

