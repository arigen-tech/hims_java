package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPatientPasswordRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must contain 10 to 15 digits")
    private String mobileNo;

    @NotBlank
    private String sessionId;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4,8}$", message = "OTP must contain 4 to 8 digits")
    private String otp;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must contain 8 to 72 characters")
    private String newPassword;
}
