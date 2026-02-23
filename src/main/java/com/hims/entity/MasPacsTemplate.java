package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mas_pacs_template", schema = "public")
public class MasPacsTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pacs_template_id", nullable = false)
    private Long pacsTemplateId;

    @NotBlank(message = "templateCode is required")
    @Size(max = 30, message = "templateCode max length is 30")
    @Column(name = "template_code", length = 30, nullable = false)
    private String templateCode;

    @NotBlank(message = "templateName is required")
    @Size(max = 150, message = "templateName max length is 150")
    @Column(name = "template_name", length = 150, nullable = false)
    private String templateName;

    @NotNull(message = "subChargecodeId is required")
    @Positive(message = "subChargecodeId must be positive")
    @ManyToOne
    @JoinColumn(name = "sub_chargecode_id", nullable = false)
    private MasSubChargeCode subChargecodeId;

    @NotBlank(message = "templateText is required")
    @Column(name = "template_text", columnDefinition = "text", nullable = false)
    private String templateText;

    @NotBlank(message = "status is required")
    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @NotBlank(message = "createdBy is required")
    @Size(max = 100, message = "createdBy max length is 100")
    @Column(name = "created_by", length = 100, nullable = false)
    private String createdBy;

    @Size(max = 100, message = "lastUpdatedBy max length is 100")
    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

}