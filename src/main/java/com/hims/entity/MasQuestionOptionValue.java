package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mas_question_option_value")
public class MasQuestionOptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qa_option_value_id")
    private Long id;

    @Column(name = "qa_option_code", length = 8, nullable = false)
    private String optionCode;

    @Column(name = "qa_option_value", length = 1000, nullable = false)
    private String optionValue;

    @Column(name = "option_score")
    private Integer optionScore;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private MasQuestion questionId;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}