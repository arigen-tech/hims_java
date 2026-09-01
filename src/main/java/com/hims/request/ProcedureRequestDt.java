package com.hims.request;

import java.time.LocalDateTime;
import java.util.List;

public class ProcedureRequestDt {

    private Long procedureHdId;
    private Long procedureId;
    private String sequenceNo;
    private Integer plannedSessionCount;
    private Integer completedSessionCount;
    private Long procedureStatusId;
    private String billingMethod;
    private String billingStatus;
    private String remarks;
    private String status;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedDate;
    private List<ProcedureSessionRequest> procedureSessionRequests;
    private DentalProcedureRequest dental;

}
