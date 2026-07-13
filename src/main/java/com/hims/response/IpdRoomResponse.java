package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IpdRoomResponse {
    private Long roomId;
    private String roomName;
}
