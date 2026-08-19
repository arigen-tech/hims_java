package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ip_adverse_event", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpAdverseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adverse_event_id", nullable = false)
    private Long adverseEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient  inpatientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private MasStoreItem medicationId;

    @Column(name = "reaction", nullable = false, columnDefinition = "TEXT")
    private String reaction;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "reaction_datetime", nullable = false)
    private LocalDateTime reactionDatetime;

    @Column(name = "medication_stopped", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String medicationStopped;

    @Column(name = "doctor_informed", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String doctorInformed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "informed_doctor_id")
    private User informedDoctorId;

    @Column(name = "patient_condition_after", columnDefinition = "TEXT")
    private String patientConditionAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private MasRoute routeId;

    @Column(name = "dose", length = 50)
    private String dose;

    @Column(name = "recorded_by", nullable = false)
    private Long recordedBy;

    @Column(name = "recorded_datetime")
    private LocalDateTime recordedDatetime;

    @Column(name = "status", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String status;
}