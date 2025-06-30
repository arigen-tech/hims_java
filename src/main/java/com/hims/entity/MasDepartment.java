package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "mas_department")
public class MasDepartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id", nullable = false)
    private Long id;

    @Size(max = 350)
    @Column(name = "department_code", length = 350)
    private String departmentCode;

    @Size(max = 350)
    @Column(name = "department_name", length = 350)
    private String departmentName;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status;

    @Size(max = 12)
    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 10)
    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_type_id")
    private MasDepartmentType departmentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospital;

    @Size(max = 50)
    @Column(name = "department_no", length = 50)
    private String departmentNo;

}
