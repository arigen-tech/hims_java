package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "mas_procedure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_id")
    private Long procedureId;

    @Column(name = "procedure_code", length = 8)
    private String procedureCode;

    @Column(name = "procedure_name", length = 30)
    private String procedureName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 250)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "department_id")
//    private MasDepartment department;

    @Column(name = "opd_allowed", length = 1)
    private String opdAllowed;

    @Column(name = "ipd_allowed", length = 1)
    private String ipdAllowed;

    @Column(name = "is_nursing", length = 1)
    private String isNursing;


}