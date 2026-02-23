package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentReqDaysKeys {
    private Long id;
    private   String day;
    private Integer tokenStartNo;
    private Integer tokenInterval;
    private   Integer totalToken;
    private Integer totalOnlineToken;
    private Integer maxNoOfDay;
    private Integer minNoOfday;
    private String opdLocation;
    private   String startTime;
    private   String endTime;
}
