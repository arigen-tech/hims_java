package com.hims.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

    @Data
    public class MasTpaRequest {

        @NotBlank(message = "TPA Name is required")
        private String tpaName;

        private String tpaCode;

        private String contactPerson;

        private String contactNo;

        @Email(message = "Invalid email format")
        private String emailId;

        private String address;
}
