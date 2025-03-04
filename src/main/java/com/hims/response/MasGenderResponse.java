package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasGenderResponse {
    private Long id;
    private String genderCode;
    private String genderName;
    private String code;
    private String status;

}
