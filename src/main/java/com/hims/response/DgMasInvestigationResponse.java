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
    private String investigationName;
    private String status;
    private String genderApplicable;
    private Double price;

    public DgMasInvestigationResponse(String investigationName, String status, String genderApplicable, Double price) {
        this.investigationName = investigationName;
        this.status = status;
        this.genderApplicable = genderApplicable;
        this.price = price;
    }
    public DgMasInvestigationResponse() {

    }


}
