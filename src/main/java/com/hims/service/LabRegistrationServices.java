package com.hims.service;

import com.hims.request.*;
import com.hims.response.*;

import java.time.LocalDate;
import java.util.List;

public interface LabRegistrationServices {


    ApiResponse<LabRadiologyRegistrationResponse> registerAndBookingLaboratory(LabRadioRegistrationRequest investigationReq);

    ApiResponse<AppsetupResponse> updateDetailsAndBookingLaboratory(LabRadioUpdateRequest labreq);

    ApiResponse<AppsetupResponse> labRegForExistingOrder(LabBillingOnlyRequest labReq);
}
