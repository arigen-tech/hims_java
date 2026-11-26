package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ot_surgery_anaesthesia", schema = "public", indexes =
        @Index(name = "idx_ot_surgery_anaesthesia_execution", columnList = "ot_surgery_execution_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtSurgeryAnaesthesia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_surgery_anaesthesia_id", nullable = false)
    private Long otSurgeryAnaesthesiaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_surgery_execution_id", nullable = false)
    private OtSurgeryExecution otSurgeryExecution;

    @Column(name = "anaesthesia_type_id")
    private Long anaesthesiaTypeId;

    @Column(name = "anaesthetist_id")
    private Long anaesthetistId;

    @Column(name = "assistant_anaesthetist_id")
    private Long assistantAnaesthetistId;

    @Column(name = "anaesthesia_start_datetime")
    private LocalDateTime anaesthesiaStartDatetime;

    @Column(name = "anaesthesia_end_datetime")
    private LocalDateTime anaesthesiaEndDatetime;

    @Column(name = "airway_management", columnDefinition = "text")
    private String airwayManagement;

    @Column(name = "asa_grade_id")
    private Long asaGradeId;

    @Column(name = "pre_induction_bp", length = 20)
    private String preInductionBp;

    @Column(name = "pre_induction_pulse")
    private Integer preInductionPulse;

    @Column(name = "pre_induction_spo2", precision = 5, scale = 2)
    private BigDecimal preInductionSpo2;

    @Column(name = "anaesthesia_notes", columnDefinition = "text")
    private String anaesthesiaNotes;

    @Builder.Default
    @Column(name = "complication", nullable = false, length = 1)
    private String complication = "N";

    @Column(name = "complication_remarks", columnDefinition = "text")
    private String complicationRemarks;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 1)
    private String status = "Y";

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @UpdateTimestamp
    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
