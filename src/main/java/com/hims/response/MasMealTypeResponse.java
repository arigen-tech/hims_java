package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasMealTypeResponse {
    private Long mealTypeId;
    private String mealTypeName;
    private Integer sequenceNo;
    private String status;
    private LocalDateTime lastUpdateDate;
//    private String createdBy;
//    private String lastUpdatedBy;


}
