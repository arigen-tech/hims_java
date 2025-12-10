package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MasBedTypeResponse {

    private Long bedTypeId;

    private String bedTypeName;


    private String description;


    private String status;


    private LocalDate lastUpdateDate;


    private String createdBy;


    private String lastUpdatedBy;
}
