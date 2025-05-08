package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "package_investigation_mapping")
public class PackageInvestigationMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long pimId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private DgInvestigationPackage packageId;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(name = "investigation_id")
    private DgMasInvestigation investId;

    @Size(max = 1)
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Size(max = 200)
    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Size(max = 200)
    @Column(name = "updated_by", length = 10)
    private String updatedBy;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

}
