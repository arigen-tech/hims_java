package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HospitalBillingConfigResponse {

    private boolean radioBillingEnabled;
    private boolean labBillingEnabled;
    private boolean medicineBillingEnabled;
}
