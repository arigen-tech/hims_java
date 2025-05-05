package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasGenderResponse {
    private Long id;
    private String genderCode;
    private String genderName;
    private LocalDateTime lastChgDt;
    private String status;
    private String code;
    private String lastChgBy;
}
