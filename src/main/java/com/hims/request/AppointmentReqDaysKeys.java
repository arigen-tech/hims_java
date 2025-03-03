package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentReqDaysKeys {
    String tokenStartNo;
    String tokenInterval;
    String totalToken;
    String totalOnlineToken;
    String maxNoOfDay;
    String minNoOfday;
}
