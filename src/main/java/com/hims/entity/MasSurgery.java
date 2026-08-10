package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_surgery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasSurgery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "surgery_id")
    private Long surgeryId;

    @Column(name = "surgery_code", length = 20)
    private String surgeryCode;

    @Column(name = "surgery_name", length = 200)
    private String surgeryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surgery_type_id")
    private MasSurgeryType surgeryType;

    @Column(name = "surgery_level", length = 3)
    private String surgeryLevel;

    @Column(name = "is_anesthesia_required", length = 1)
    private String isAnesthesiaRequired;

    @Column(name = "is_admission_required", length = 1)
    private String isAdmissionRequired;

    @Column(name = "is_implant_required", length = 1)
    private String isImplantRequired;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_updated_by", length = 300)
    private String lastUpdatedBy;

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;
}