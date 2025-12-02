package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasRoomRequest {

    private String roomName;
    private Integer noOfBeds;
    private Long wardId;
    private Long roomCategoryId;

}
