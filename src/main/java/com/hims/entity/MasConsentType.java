package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mas_consent_type", schema = "public")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MasConsentType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consent_type_id")
    private Long consentTypeId;
    @Column(name = "consent_type_code", nullable = false, length = 50, unique = true)
    private String consentTypeCode;
    @Column(name = "consent_type_name", nullable = false, length = 200)
    private String consentTypeName;
    @Column(name = "status", nullable = false, length = 1)
    private String status;
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;
    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
