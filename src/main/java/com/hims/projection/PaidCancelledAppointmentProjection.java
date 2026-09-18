//package com.hims.projection;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//public interface PaidCancelledAppointmentProjection {
//
//    Long getVisitId();
//    Long getPatientId();
//    Long getBillingHeaderId();
//    String getRegistrationNo();
//    String getPatientName();
//    String getMobileNo();
//    String getAge();
//    String getGender();
//    String getBillingType();
//    LocalDateTime getDate();
//    Long getBillingAmount();
//    LocalDateTime getCancelledDate();
//
//    Long getPaymentId();
//    Long getRefundId();
//
//    LocalDateTime getRefundDate();
//    String getRefundStatus();
//    String getDepartmentName();
//}

package com.hims.projection;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface PaidCancelledAppointmentProjection {

    Long getVisitId();

    Long getPatientId();

    Long getBillingHeaderId();

    String getPatientName();

    String getMobileNo();

    String getMobileNumber();

    String getAge();

    String getGender();

    Long getDoctorId();

    String getDoctorName();

    Long getDepartmentId();

    LocalDate getAppointmentDate();

    String getAppointmentTime();

    Instant getCancellationDateTime();

    String getCancelledBy();

    String getCancellationReason();

    String getBillingType();

    LocalDateTime getDate();

    Instant getBillDate();

    LocalDateTime getCancelledDate();

    Long getBillingAmount();

    Long getPaymentId();

    Long getRefundId();

    BigDecimal getRefundAmount();

    String getRefundReferenceNo();

    String getRefundReason();

    String getGatewayRefundId();

    LocalDateTime getRefundDate();

    String getRefundStatus();

    Long getPaymentModeId();

    String getPaymentModeCode();

    String getPaymentModeName();

    String getDepartmentName();
}