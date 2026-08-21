package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasAnaesthesiaTypeResponse {

    private Long anaesthesiaTypeId;
    private String anaesthesiaTypeCode;
    private String anaesthesiaTypeName;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
