package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_employment_type")
@Getter
@Setter
public class MasEmploymentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "employment_type", length = 50, nullable = false)
    private String employmentType;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChangedBy;

    @Column(name = "last_chg_dt")
    private LocalDateTime lastChangedDate = LocalDateTime.now();
}
