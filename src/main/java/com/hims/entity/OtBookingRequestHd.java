package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "ot_booking_request_hd")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtBookingRequestHd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_booking_request_id")
    private Long otBookingRequestId;

    @Column(name = "request_no", length = 50)
    private String requestNo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patientId;

    @Column(name = "request_source", length = 10)
    private String requestSource;

    @Column(name = "visit_id")
    private Long visitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private MasDepartment departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_surgeon_id")
    private User primarySurgeonId;

    @Column(name = "icd_code_id")
    private Long icdCodeId;

    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @Column(name = "priority", length = 20)
    private String priority;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_ot_id")
    private MasOperationTheatre preferredOtId;

    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Column(name = "preferred_start_time")
    private LocalTime preferredStartTime;

    @Column(name = "preferred_end_time")
    private LocalTime preferredEndTime;

    @Column(name = "special_instruction", length = 1000)
    private String specialInstruction;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_status_id")
    private MasOtBookingStatus bookingStatusId;

    @Column(name = "requested_by", length = 200)
    private String requestedBy;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(name = "reviewed_by", length = 200)
    private String reviewedBy;

    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @Column(name = "rejection_remarks", length = 1000)
    private String rejectionRemarks;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id")
    private Inpatient admissionId;

}
