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
public class ProcedureDetailResponse {

    private Long procedureDtId;
    private Long procedureId;
    private List<Long> sessionIds;
}
