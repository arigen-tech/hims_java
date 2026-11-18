package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ot_checklist_item", schema = "public",
        uniqueConstraints = @UniqueConstraint(name = "uk_ot_checklist_item", columnNames = {"ot_checklist_id", "checklist_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtChecklistItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_checklist_item_id")
    private Long otChecklistItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_checklist_id", nullable = false)
    private OtChecklist otChecklist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", nullable = false)
    private MasOtChecklist checklist;

    @Column(name = "verification_status", length = 3)
    private String verificationStatus;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "verified_by", length = 200)
    private String verifiedBy;

    @Column(name = "verified_datetime")
    private LocalDateTime verifiedDatetime;
}
