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
public class MasIntakeItemResponse {

    private Long intakeItemId;
    private Long intakeTypeId;
    private String intakeTypeName;
    private String intakeItemName;
    private String status;
    private LocalDateTime lastUpdateDate;
//    private String createdBy;
//    private String lastUpdatedBy;
}
