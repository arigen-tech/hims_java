package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IpProcedureTxnResponse {
    private Long procedureTxnId;
    private Long inpatientId;
    private String procedureName;
    private LocalDateTime procedureDatetime;
    private String performedBy;
    private String remarks;
}
