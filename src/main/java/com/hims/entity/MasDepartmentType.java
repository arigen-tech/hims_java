package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mas_department_type")
public class MasDepartmentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_type_id", nullable = false)
    private Long id;

    @Size(max = 8)
    @Column(name = "department_type_code", length = 8)
    private String departmentTypeCode;

    @Size(max = 30)
    @Column(name = "department_type_name", length = 30)
    private String departmentTypeName;

    @Size(max = 1)
    @NotNull
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

}
