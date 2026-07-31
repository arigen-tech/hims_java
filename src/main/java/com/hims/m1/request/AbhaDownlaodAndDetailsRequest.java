package com.hims.m1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class AbhaDownlaodAndDetailsRequest {


    @NotBlank(message = "x-Token cannot be null or empty.")
    String xToken;

    @NotBlank(message = "IsType cannot be null or empty.")
    String isType;

}
