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
public class MasQuestionHeadingResponse {

    private Long questionHeadingId;
    private String questionHeadingCode;
    private String questionHeadingName;
    private String status;
    private LocalDateTime lastUpdateDate;
}
