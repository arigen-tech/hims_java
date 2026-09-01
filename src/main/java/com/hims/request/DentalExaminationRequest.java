package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentalExaminationRequest {

    private Integer totalTeeth;
    private Integer missingTeeth;
    private Integer unsalvageableTeeth;
    private Integer affectedTeeth;
    private Integer dentalDiseaseScore;
    private String notes;
    private Integer ongoingProcedures;
    private List<DentalToothConditionRequest> toothConditions;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DentalToothConditionRequest {
        private Long toothId;
        private Long conditionId;
    }
}