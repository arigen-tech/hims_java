package com.hims.m1.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MasterResponse {

    String id;
    String master_name;
    String master_description;

}