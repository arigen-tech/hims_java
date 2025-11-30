package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@Table(name="mas_investigation_methodology")
public class MasInvestigationMethodology {

    @Column(name="method_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long methodId;

    @Column(name="method_name",length=100)
    private String methodName;

    @Column(name="notes",length=100)
    private String note;



    @Column(name="last_chg_by",length=100)
    private String lastChgBy;

    @Column(name="last_chg_date")
    private LocalDate lastChgDate;


}
