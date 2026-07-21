package com.hims.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InpatientBookingInvestigation {
    private Long patientId;
    private Long inPatientId;
    private Long investigationId;
    private String sample;
    private String container;
    private String resultUnit;
    private String remarks;
}