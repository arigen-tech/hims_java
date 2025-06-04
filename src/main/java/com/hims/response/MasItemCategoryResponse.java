package com.hims.response;

import com.hims.entity.MasStoreSection;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MasItemCategoryResponse {

    private Integer itemCategoryId;


    private String itemCategoryCode;


    private String itemCategoryName;


    private String status;


    private String lastChgBy;


    private LocalDate lastChgDate;


    private String lastChgTime;


    private Integer sectionId;

}
