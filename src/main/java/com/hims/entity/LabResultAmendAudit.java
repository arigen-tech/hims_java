package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lab_result_amend_audit")
@Entity
public class LabResultAmendAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amendment_id")
    private Long amendmentId;

    @Column(name = "sample_generated_id",length = 100,nullable = false)
    private String generatedSampleId;

    @Column(name = "old_result",length = 100,nullable = false)
    private String oldResult;

    @Column(name = "new_result",length = 100,nullable = false)
    private String newResult;

    @Column(name = "reason_for_change",length =200 )
    private String reasonForChange;

    @Column(name = "amended_by",length = 100)
    private String amendedBy;

    @Column(name = "amended_datetime")
    private LocalDateTime amendedDatetime;

    @Column(name = "remarks",columnDefinition = "text")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amendment_type_id")
    private MasLabResultAmendmentType amendmentType;



}
