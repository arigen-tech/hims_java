package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MasWardCategoryResponse {
    private Long categoryId;
    private String categoryName;
    private  String description;
    private  String status;
    private LocalDate lastUpdateDate;
    private String createdBy;
    private  String  LastUpdatedBy;
    private Long careId;
    private String careLevelName;
}
