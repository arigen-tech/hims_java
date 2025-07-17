package com.hims.response;

import com.hims.entity.MasDepartment;
import com.hims.entity.MasHospital;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreStockTakingMResponse {

    private Long takingMId;
    private LocalDateTime physicalDate;
    private String reason;
    private String stockTakingNo;
    private String approvedBy;
    private LocalDateTime approvedDt;
    private Long hospitalId;
    private Long departmentId;
    private LocalDateTime lastChgDate;
    private String status;
    private String createdBy;
    List<StoreStockTakingTResponse> storeStockTakingTResponseList;
}

