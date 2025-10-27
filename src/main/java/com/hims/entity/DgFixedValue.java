package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table (name = "dg_fixed_value")
public class DgFixedValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fixed_id")
    private Long fixedId;

    @Column(name = "fixed_value")
    private String fixedValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_investigation_id")
    private DgSubMasInvestigation subInvestigationId;
}
