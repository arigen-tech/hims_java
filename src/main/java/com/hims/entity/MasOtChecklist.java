package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_ot_checklist", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasOtChecklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private Long checklistId;

    @Column(name = "checklist_stage", nullable = false, length = 20)
    private String checklistStage;

    @Column(name = "checklist_code", nullable = false, length = 50, unique = true)
    private String checklistCode;

    @Column(name = "checklist_name", nullable = false, length = 250)
    private String checklistName;

    @Column(name = "is_mandatory", nullable = false, length = 1)
    private String isMandatory;

    @Column(name = "allow_na", nullable = false, length = 1)
    private String allowNa;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
