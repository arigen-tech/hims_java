package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MasRoomResponse {

    private Long roomId;
    private String roomName;
    private  String status;
    private LocalDate lastUpdatedDate;
    private Long departmentId;
    private String wardName;
    private Long roomCategoryId;
    private String roomCategoryName;
    private Integer noOfBeds;
}
