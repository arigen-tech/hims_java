package com.hims.request;

import lombok.Data;

@Data
public class MasWardCategoryRequest {
    private String categoryName;
    private String description;
    private Long careId;
}
