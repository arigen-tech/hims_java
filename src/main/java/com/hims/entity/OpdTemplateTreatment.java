package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "opd_template_treatment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpdTemplateTreatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_template_id")
    private Long treatmentid;

    @Column(name = "dosage", length = 100)
    private String dosage;

    @Column(name = "noofdays")
    private Long noOfDays;

    @Column(name = "total")
    private Long total;

    @Column(name = "instruction", length = 200)
    private String instruction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frequency_id")
    private MasFrequency frequency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private MasStoreItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private OpdTemplate template;
}

