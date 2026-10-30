package com.hims.m2.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CareContextLinkRequest {

    @NotBlank private String abhaAddress;
    private String abhaNumber;
    @NotBlank private String patientId;
    @NotBlank private String patientName;
    @NotBlank private String gender;
    private Integer yearOfBirth;

    @Valid
    @NotEmpty
    private List<Ctx> careContexts;

    @Data
    public static class Ctx {
        @NotBlank private String referenceNumber;
        @NotBlank private String display;
        private String hiType;               // e.g. OPConsultation, DiagnosticReport (VERIFY)
    }
}
