package com.hims.projection;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PendingTrackingIPDBillProjection {

    Long getInpatientId();

    Long getBillingHeaderId();

    String getUhid();

    String getPatientName();

    String getAge();

    Long getGenderId();

    String getGender();

    String getMobileNo();

    String getAdmissionNo();

    Long getWardId();

    String getWard();

    Long getRoomId();

    String getRoom();

    Long getBedId();

    String getBed();

    LocalDateTime getAdmissionDateTime();

    Long getBillingTypeId();

    String getBillingType();

    BigDecimal getTotalAmount();

    BigDecimal getEstimationCost();

    BigDecimal getPatientPaid();

    BigDecimal getOutStandingAmount();

    Long getBillStatusId();

    String getBillStatus();

    Long getPaymentStatusId();

    String getPaymentStatus();
}