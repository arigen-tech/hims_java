package com.hims.request;


import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasPatientConditionRequest {

    @Size(max = 50, message = "Patient condition name should not exceed 50 characters")
    private String patientConditionName;

    @Size(max = 200, message = "Description should not exceed 200 characters")
    private String description;

}