package com.hims.response;

import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasIpNursingAssessmentValueResponse {

    private Long assessmentValueId;

    private String categoryCode;

    private String valueCode;

    private String valueName;

    private Integer displayOrder;

    private String status;
}