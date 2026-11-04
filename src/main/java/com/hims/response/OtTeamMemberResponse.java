package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class OtTeamMemberResponse {
    private Long otBookingId;
    private List<OtTeamMemberDetailResponse> teamMembers;
}
