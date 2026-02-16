package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static com.hims.constants.AppConstants.*;

@Getter
@Setter
@Entity
@Table(name = "mas_main_chargecode")
public class MasMainChargeCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "main_chargecode_id", nullable = false)
    private Long chargecodeId;

    @Size(max = 8)
    @Column(name = "main_chargecode_code", nullable = false, length = 8)
    private String chargecodeCode;

    @Size(max = 30)
    @Column(name = "main_chargecode_name", nullable = false, length = 30)
    private String chargecodeName;

    @Size(max = 1)
    @Column(name = "status", nullable = false, length = 1)
    @Pattern(regexp = STATUS_PATTERN, message = "Status must be y or n")
    private String status = STATUS_ACTIVE;

    @Size(max = 12)
    @Column(name = "last_chg_by", nullable = false, length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDate lastChgDate;

    @Column(name = "last_chg_time", nullable = false)
    private String lastChgTime;

//    @Size(max = 1)
//    @Column(name = "maincharge_type", length = 1)
//    private char mainChargeType;
//
//    @Column (name = "department_id", nullable = false)
//    private int departmentId;
}
