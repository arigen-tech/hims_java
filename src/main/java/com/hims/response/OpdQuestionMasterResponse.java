package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OpdQuestionMasterResponse {

    private Long id;
    private String question;
    private Long questionHeadingId;
    private String questionHeadingName;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}