package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasAnaesthesiaTypeResponse {

    private Long anaesthesiaTypeId;
    private String anaesthesiaTypeCode;
    private String anaesthesiaTypeName;
    private BigDecimal price;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
