package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PacsHmisStudyResponse {
    private Long id;
    private String orderNo;
    private String uhid;
    private String studyInstanceUid;
    private String modality;
    private String studyDescription;
    private LocalDateTime studyDatetime;
    private String studyStatus;
    private String pacsSource;
}
