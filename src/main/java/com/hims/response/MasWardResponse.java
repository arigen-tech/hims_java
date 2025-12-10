package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MasWardResponse {
    private Long wardId;

    private String wardName;

    private Long wardCategoryId;
    private String wardCategoryName;

    private Long careLevelId;
    private String careLevelName;

    private String status;

    private LocalDate lastUpdateDate;

    private String createdBy;

    private String lastUpdatedBy;
}
