package com.hims.response;

import lombok.Data;

import java.time.Instant;
@Data
public class MasItemTypeResponse {

    private int id;


    private String code;


    private String name;


    private String status;


    private String lastChgBy;


    private Instant lastChgDate;


    private String lastChgTime;


    private Integer masStoreGroupId;

    private String masStoreGroupName;

}
