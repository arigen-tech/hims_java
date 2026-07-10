package com.hims.m1.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProfileRequest {



    @NotBlank(message = "Profile photo cannot be null or empty.")
    String profilePhoto;

    @NotBlank(message = "X-token photo cannot be null or empty.")
    String xToken;


}
