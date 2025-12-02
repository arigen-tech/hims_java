package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MasBedStatusResponse {

    private Long bedStatusId;
    private  String bedStatusName;
    private String status;
    private LocalDate lastUpdateDate;


}
