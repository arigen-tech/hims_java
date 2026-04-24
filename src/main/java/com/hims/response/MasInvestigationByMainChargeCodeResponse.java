package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasInvestigationByMainChargeCodeResponse {
    private Long investigationId;
    private String investigationName;
   private Long mainChargeCodeId;



}
