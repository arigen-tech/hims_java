package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_compatibility")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasBloodCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compatibility_id")
    private Long compatibilityId;

     @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private MasBloodComponent componentId;

     @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_blood_group_id")
    private MasBloodGroup patientBloodGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_blood_group_id")
    private MasBloodGroup donorBloodGroupId;

    @Column(name = "is_preferred")
    private String isPreferred;

    @Column(name = "status")
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
