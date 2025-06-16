package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class MasItemCategoryRequest {
    private String itemCategoryCode;
    private String itemCategoryName;
    private Integer sectionId;


}

