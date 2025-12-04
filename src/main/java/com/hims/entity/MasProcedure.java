package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mas_procedure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasProcedure {

    @Id
    @Column(name = "procedure_id")
    private Integer procedureId;

    @Column(name = "procedure_code", length = 8)
    private String procedureCode;

    @Column(name = "procedure_name", length = 30)
    private String procedureName;

    @Column(name = "defaultstatus", length = 1, nullable = false)
    private String defaultStatus;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "last_chg_by", length = 12)
    private String lastChangedBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChangedDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChangedTime;

    @Column(name = "procedure_group", length = 2)
    private String procedureGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private MasDepartment department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_type_id")
    private MasProcedureType procedureType;
}
