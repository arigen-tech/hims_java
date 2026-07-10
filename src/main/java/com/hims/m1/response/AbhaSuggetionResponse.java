package com.hims.m1.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbhaSuggetionResponse {


    String isType;
    String tnxId;
    List<String> abhaAddressList;

}
