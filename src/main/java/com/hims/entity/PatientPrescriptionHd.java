package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_prescription_hd")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientPrescriptionHd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_hd_id")
    private Long prescriptionHdId;

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @Column(name = "nis_no",length = 100)
    private String nisNo;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_name", length = 200)
    private String doctorName;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "prescription_date", nullable = false)
    private LocalDateTime prescriptionDate = LocalDateTime.now();

    @Column(name = "status", nullable = false, length = 1)
    private String status = "N";

    @Column(name = "billing_status", nullable = false, length = 1)
    private String billingStatus;

    @Column(name = "created_by", nullable = false, length = 200)
    private String createdBy;

    @Column(name = "issued_by", length = 200)
    private String issuedBy;

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "total_gst", precision = 12, scale = 2)
    private BigDecimal totalGst = BigDecimal.ZERO;

    @Column(name = "total_discount", precision = 12, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;

    // --- Optional relationships (uncomment if you have these entities) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", insertable = false, updatable = false)
    private MasHospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private MasDepartment department;

}
