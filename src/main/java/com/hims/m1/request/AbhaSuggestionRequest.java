package com.hims.m1.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AbhaSuggestionRequest {



    @NotBlank(message = "TnxID cannot be null or empty.")
    String tnxId;


}
