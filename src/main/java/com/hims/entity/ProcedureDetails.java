package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "procedure_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureDetails {

    @Id
    @Column(name = "procedure_details_id")
    private Integer procedureDetailsId; // No auto-id

    @Column(name = "remarks", length = 100)
    private String remarks;

    @Column(name = "procedure_name", length = 35)
    private String procedureName;

    @Column(name = "status", length = 1)
    private String status;

    // FK → procedure_header
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_header_id")
    private ProcedureHeader procedureHeader;

    // FK → mas_procedure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id")
    private MasProcedure masProcedure;

    @Column(name = "frequency_id")
    private Integer frequencyId;

    @Column(name = "no_of_days")
    private Integer noOfDays;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "final_procedure_status", length = 1)
    private String finalProcedureStatus;

    @Column(name = "nursing_remark", length = 200)
    private String nursingRemark;

    @Column(name = "next_appointment_date")
    private LocalDate nextAppointmentDate;

    @Column(name = "appointment_time", length = 10)
    private String appointmentTime;

    @Column(name = "procedure_date")
    private LocalDate procedureDate;

    @Column(name = "procedure_time", length = 10)
    private String procedureTime;
}
