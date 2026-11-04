package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class OtTeamMemberRequest {

    @NotNull
    @Positive
    private Long otBookingId;

    @Valid
    @NotEmpty
    private List<OtTeamMemberDetailRequest> teamMembers;
}
