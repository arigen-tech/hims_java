package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentalDetailsResponse {

    private Long summaryId;

    private Long patientId;
    private Long visitId;
    private List<DentalProcedureResponse> procedureHdDetails;
    private DentalExaminationResponse dentalExamination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DentalExaminationResponse {

        private Long summaryId;

        private Integer totalTeeth;
        private Integer missingTeeth;
        private Integer unsalvageableTeeth;
        private Integer affectedTeeth;
        private Integer dentalDiseaseScore;
        private String notes;
        private Integer ongoingProcedures;

        private List<DentalToothConditionResponse> toothConditions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DentalToothConditionResponse {
        private Long toothPatientConditionId;
        private Long toothId;
        private Long conditionId;
    }
}