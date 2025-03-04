package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "role_template")
public class RoleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_template_id_gen")
    @Column(name = "role_template_id", nullable = false)
    private Long id;

    @Column(name = "role_id")
    private Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private MasTemplate template;

    @Size(max = 4)
    @Column(name = "status", length = 4)
    private String status;

    @Column(name = "last_chg_by")
    private Long lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospital;

    @Column(name = "mmu_id")
    private Long mmuId;

}