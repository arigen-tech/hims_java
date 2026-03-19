package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasQuestionOptionValueResponse {

    private Long id;
    private String optionCode;
    private String optionValue;
    private Integer optionScore;
    private String status;
    private Long questionId;
    private String questionName;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}