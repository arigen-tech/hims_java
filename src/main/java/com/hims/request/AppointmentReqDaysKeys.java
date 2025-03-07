package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentReqDaysKeys {
    Long id;
    String day;
    Integer tokenStartNo;
    Integer tokenInterval;
    Integer totalToken;
    Integer totalOnlineToken;
    Integer maxNoOfDay;
    Integer minNoOfday;
}
