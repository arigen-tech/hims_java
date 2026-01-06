package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lab_turn_around_time")
public class LabTurnAroundTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "turn_around_time_id")
    private Long turnAroundTimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_hd_id")
    private DgOrderHd orderHd;

    @Column(name = "sample_collection_date_time")
    private LocalDateTime sampleCollectionDateTime;

    @Column(name = "sample_collected_by",length = 100)
    private String sampleCollectedBy;

    @Column(name = "sample_validation_date_time")
    private LocalDateTime sampleValidatedDateTime;

    @Column(name = "sample_validated_by",length = 100)
    private String sampleValidatedBy;

    @Column(name = "result_entry_date_time")
    private LocalDateTime resultEntryDateTime;

    @Column(name = "result_entered_by",length = 100)
    private String resultEnteredBy;

    @Column(name = "result_validation_date_time")
    private LocalDateTime resultValidationTime;

    @Column(name = "result_validated_by",length = 100)
    private String resultValidatedBy;

    @Column(name = "is_reject")
    private Boolean isReject;

    private String generatedSampleId;
}
