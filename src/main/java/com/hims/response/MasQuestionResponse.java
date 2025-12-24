package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MasQuestionResponse {

    private Long id;
    private String question;
    private Long questionHeadingId;
    private  String  questionHeadingName;
    private Integer optionValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
