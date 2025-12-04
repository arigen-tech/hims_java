package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "procedure_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureHeader {

    @Id
    @Column(name = "procedure_header_id")
    private Integer procedureHeaderId;  // No identity → manually assigned

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_date")
    private LocalDate lastChangedDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChangedTime;

    @Column(name = "requisition_date")
    private LocalDate requisitionDate;

    @Column(name = "procedure_date")
    private LocalDateTime procedureDate;

    @Column(name = "procedure_time", length = 10)
    private String procedureTime;

    @Column(name = "hin_id")
    private Integer hinId;

    // FK → mas_hospital.hospital_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospital;

    @Column(name = "last_chg_by")
    private Integer lastChangedBy;

    @Column(name = "medical_officer_id")
    private Integer medicalOfficerId;

    @Column(name = "visit_id")
    private Integer visitId;

    @Column(name = "dma_register_id")
    private Integer dmaRegisterId;

    @Column(name = "inpatient_id")
    private Integer inpatientId;

    @Column(name = "opd_patient_details_id")
    private Integer opdPatientDetailsId;

    @Column(name = "procedure_type", length = 2)
    private String procedureType;
}
