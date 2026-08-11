package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "mas_procedure_consumable_template",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_proc_template_code",
                        columnNames = "template_code"
                ),
                @UniqueConstraint(
                        name = "uk_proc_template_name",
                        columnNames = {"procedure_id", "template_name"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasProcedureConsumableTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false, foreignKey = @ForeignKey(name = "fk_proc_template"))
    private MasProcedure procedure;

    @Column(name = "template_code", length = 20, nullable = false, unique = true)
    private String templateCode;

    @Column(name = "template_name", length = 200, nullable = false)
    private String templateName;

    @Column(name = "is_default")
    private String isDefault;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;
}