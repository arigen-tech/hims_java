package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "discharge_icd_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DischargeIcdCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discharge_icd_code_id")
    private Long dischargeIcdCodeId;

    @Column(name = "add_edit_date")
    private LocalDate addEditDate;

    @Column(name = "add_edit_time")
    private String addEditTime;

    @Column(name = "icd_id")
    private Long icdId;

    @Column(name = "inpatient_id")
    private Integer inpatientId;

    @Column(name = "add_edit_by_id")
    private Long addEditById;

    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "opd_patient_details_id")
    private Long opdPatientDetailsId;
}
