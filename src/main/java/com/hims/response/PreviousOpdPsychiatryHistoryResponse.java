package com.hims.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreviousOpdPsychiatryHistoryResponse {
    private Long assessmentHeaderId;
    private Long patientId;
    private Long visitId;
    private String topicName;
    private String doctorName;
    private LocalDateTime assessmentDate;
    private BigDecimal totalScore;
    private String remarks;
    private List<PsychiatricAssessmentResponse> assessments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PsychiatricAssessmentResponse {
        private String topicName;
        private List<AssessmentQuestionsResponse> questionsResponses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AssessmentQuestionsResponse {
        private String questionName;
        private String questionsAns;
    }

}
