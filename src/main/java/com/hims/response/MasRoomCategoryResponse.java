package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MasRoomCategoryResponse {

    private Long roomCategoryId;
    private String roomCategoryName;
    private String status;
    private LocalDate lastUpdatedDate;
}
