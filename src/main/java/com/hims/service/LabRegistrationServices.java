package com.hims.service;

import com.hims.entity.*;
import com.hims.request.*;
import com.hims.response.*;

import java.time.LocalDate;
import java.util.List;

public interface LabRegistrationServices {


    ApiResponse<LabRadiologyRegistrationResponse> registerAndBookingLaboratory(LabRadioRegistrationRequest investigationReq);

    ApiResponse<AppsetupResponse> updateDetailsAndBookingLaboratory(LabRadioUpdateRequest labreq);

    ApiResponse<AppsetupResponse> labRegForExistingOrder(LabBillingOnlyRequest labReq);


    DgOrderHd saveLabOrderHeader(Patient patient, Visit visit, User currentUser, LocalDate appointmentDate, boolean billingEnabled);


    DgOrderDt saveLabOrderDetail(DgOrderHd hd, BillingHeader billing, LabRadioInvestigationRequest inv,
                                 DgMasInvestigation entity, User currentUser, String serviceCategoryCode);

    DgOrderDt saveLabOrderDetailForPackage(DgOrderHd hd, BillingHeader billing, LabRadioInvestigationRequest inv,
                                           DgMasInvestigation investEntity, DgInvestigationPackage pkg,
                                           User currentUser);
}
