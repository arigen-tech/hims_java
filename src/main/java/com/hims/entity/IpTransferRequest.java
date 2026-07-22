package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "ip_transfer_request", schema = "public", uniqueConstraints = {
                @UniqueConstraint(name = "ip_transfer_request_transfer_no_key", columnNames = "transfer_no")}
)
public class IpTransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id", nullable = false)
    private Long transferId;

    @Column(name = "transfer_no", unique = true, length = 30)
    private String transferNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id",  foreignKey = @ForeignKey(name = "ip_transfer_request_patient_id_fkey"))
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id",  foreignKey = @ForeignKey(name = "fk_inpatient"))
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_ward_id",  foreignKey = @ForeignKey(name = "ip_transfer_request_from_ward_id_fkey")
    )
    private MasWard fromWard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_bed_id",  foreignKey = @ForeignKey(name = "ip_transfer_request_from_bed_id_fkey"))
    private MasBed fromBed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_ward_id",  foreignKey = @ForeignKey(name = "ip_transfer_request_to_ward_id_fkey")
    )
    private MasWard toWard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_bed_id", nullable = false, foreignKey = @ForeignKey(name = "ip_transfer_request_to_bed_id_fkey"))
    private MasBed toBed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false, foreignKey = @ForeignKey(name = "ip_transfer_request_doctor_id_fkey"))
    private User doctor;


    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(name = "request_datetime")
    private LocalDateTime requestDatetime;

    @Column(name = "requested_by", length = 300)
    private String requestedBy;


    @Column(name = "approval_required", length = 1)
    private String approvalRequired;

    @Column(name = "approved_by", length = 300)
    private String approvedBy;

    @Column(name = "approval_datetime")
    private LocalDateTime approvalDatetime;

    @Column(name = "accepted_by", length = 300)
    private String acceptedBy;

    @Column(name = "acceptance_datetime")
    private LocalDateTime acceptanceDatetime;

    @Column(name = "transfer_datetime")
    private LocalDateTime transferDatetime;

    @Column(name = "transfer_status", length = 30)
    private String transferStatus;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;


    @Column(name = "is_emergency", length = 1)
    private String isEmergency;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;


}