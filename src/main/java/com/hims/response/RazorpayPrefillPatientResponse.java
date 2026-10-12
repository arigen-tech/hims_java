package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayPrefillPatientResponse {

    private String patientFullName;
    private String email;
    private String phoneNumber;
}
