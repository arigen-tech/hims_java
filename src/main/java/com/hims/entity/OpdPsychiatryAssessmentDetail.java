package com.hims.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "opd_psychiatry_assessment_detail", schema = "public")
public class OpdPsychiatryAssessmentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_detail_id", nullable = false)
    private Long assessmentDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_header_id")
    private OpdPsychiatryAssessmentHeader assessmentHeaderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private OpdQuestionMaster questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_option_id")
    private MasQuestionOptionValue answerOptionId;

    @Column(name = "score", precision = 10, scale = 2)
    private BigDecimal score;
}