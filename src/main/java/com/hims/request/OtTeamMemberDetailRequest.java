package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OtTeamMemberDetailRequest {

    @NotNull
    @Positive
    private Long otTeamRoleId;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Pattern(regexp = "[YNyn]", message = "isPrimary must be Y or N")
    private String isPrimary;
}
