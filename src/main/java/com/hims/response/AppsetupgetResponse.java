package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppsetupgetResponse {
    String day;
    Integer tokenStartNo;
    Integer tokenInterval;
    Integer totalToken;
    Integer totalOnlineToken;
    Integer maxNoOfDay;
    Integer minNoOfday;
}
