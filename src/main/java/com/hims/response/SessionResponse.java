package com.hims.response;

import lombok.Data;

@Data
public class SessionResponse{
    private Long sessionId;
    private String startTime;
    private String endTime;
    private String day;
    private Integer minDay;
    private Integer maxDay;


}
