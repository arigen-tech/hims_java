package com.hims.response;

import lombok.Data;

@Data
public class AppSetResponse {
    private Integer minDay;
    private Integer maxDay;
    private Long session;
    private String day;
}
