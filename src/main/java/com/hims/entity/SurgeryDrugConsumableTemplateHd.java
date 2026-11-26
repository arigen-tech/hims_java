package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "surgery_drug_consumable_template_hd", schema = "public",
        indexes = @Index(name = "idx_surgery_drug_consumable_template_surgery", columnList = "surgery_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurgeryDrugConsumableTemplateHd {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "surgery_id", nullable = false)
    private MasSurgery surgery;

    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 1)
    private String status = "Y";

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @UpdateTimestamp
    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
