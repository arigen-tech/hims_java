package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureResponse {

    private Long procedureHdId;
    private String procedureNo;
    private List<ProcedureDetailResponse> procedures;
}
