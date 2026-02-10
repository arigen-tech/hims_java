package com.hims.response;

import lombok.Data;

import java.time.LocalTime;

@Data
public class SessionResponseList {
    private Long sessionId;
    private String  startTime;
    private String endTime;

}
