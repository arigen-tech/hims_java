package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mas_symptoms")
public class MasSymptoms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "symptoms_id", nullable = false)
    private long id;

    @Size(max = 7)
    @Column(name = "symptoms_code", length = 7)
    private String symptomsCode;

    @Size(max = 300)
    @Column(name = "symptoms_name", length = 300)
    private String symptomsName;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 1)
    @Column(name = "most_common_use", length = 1)
    private String mostCommonUse;
}
