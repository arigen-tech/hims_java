package com.hims.m1.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ParseErrorResponse {



    String errorMsg;
    String errorCode;


}
