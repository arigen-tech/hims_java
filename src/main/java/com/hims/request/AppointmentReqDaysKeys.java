package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentReqDaysKeys {
    String day;
    String tokenStartNo;
    String tokenInterval;
    String totalToken;
    String totalOnlineToken;
    String maxNoOfDay;
    String minNoOfday;
}
