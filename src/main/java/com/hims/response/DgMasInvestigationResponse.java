package com.hims.response;

import com.hims.entity.DgMasSample;
import com.hims.entity.DgUom;
import com.hims.entity.MasMainChargeCode;
import com.hims.entity.MasSubChargeCode;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Data
public class DgMasInvestigationResponse {
    private Long investigationId;
    private String investigationName;
    private String status;
    private String genderApplicable;
    private String investigationType;
    private Double price;

    private Long mainChargeCodeID;





    }



