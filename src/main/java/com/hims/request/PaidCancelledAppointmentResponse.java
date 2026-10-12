//package com.hims.request;
//
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
//@Data
//public class PaidCancelledAppointmentResponse {
//    private Long visitId;
//    private Long patientId;
//    private Long billingHeaderId;
//    private String registrationNo;
//    private String patientName;
//    private String mobileNo;
//    private String age;
//    private String gender;
//    private String billingType;
//    private LocalDateTime date;
//    private Long billingAmount;
//    private LocalDateTime cancelledDate;
//    private Long paymentId;
//    private  Long refundId;
//    private LocalDateTime refundDate;
//    private String refundStatus;
//    private String departmentName;
//}
package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class PaidCancelledAppointmentResponse {

    private Long visitId;
    private Long patientId;
    private Long billingHeaderId;

    private String patientName;
    private String mobileNo;
    private String mobileNumber;
    private String age;
    private String gender;

    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private Instant cancellationDateTime;
    private String cancelledBy;
    private String cancellationReason;

    private String billingType;

    private LocalDateTime date;
    private LocalDateTime billDate;
    private LocalDateTime cancelledDate;

    private Long billingAmount;

    private Long paymentId;

    private Long refundId;
    private BigDecimal refundAmount;
    private String refundReferenceNo;
    private String refundReason;
    private String gatewayRefundId;
    private LocalDateTime refundDate;
    private String refundStatus;

    private Long paymentModeId;
    private String paymentModeCode;
    private String paymentModeName;

    private String departmentName;
}
