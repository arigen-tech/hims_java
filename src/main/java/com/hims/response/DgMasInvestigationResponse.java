package com.hims.response;

import com.hims.entity.*;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DgMasInvestigationResponse {
    private Long investigationId;
    private String investigationName;
    private String status;
    private String confidential;
    private String appearInDischargeSummary;
    private String investigationType;
    private String multipleResults;
    private String quantity;
    private String normalValue;
    private String lastChgBy;
    private Instant lastChgDate;
    private String lastChgTime;
    private String appointmentRequired;
    private String maxNormalValue;
    private String minNormalValue;
    private Long testOrderNo;
    private String numericOrString;
    private String hicCode;
    private Long mainChargeCodeId;
    private String mainChargeCodeName;
    private Long uomId;
    private String uomName;
    private Long categoryId;
    private String categoryName;
    private Long methodId;
    private String methodName;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private Long sampleId;
    private String sampleName;
    private String equipmentId;
    private Long collectionId;
    private String collectionName;
    private String bloodReactionTest;
    private String bloodBankScreenTest;
    private String instructions;
    private String discountApplicable;
    private String genderApplicable;
    private String discount;
    private Double price;
    private String interpretation;
    private List<DgSubMasInvestigationResponse> subInvestigationResponseList;
    private List<DgFixedValueResponse> fixedValueResponseList;
    private List<DgNormalValueResponse> normalValueResponseList;
}



