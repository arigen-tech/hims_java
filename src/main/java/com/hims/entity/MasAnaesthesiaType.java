package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mas_anaesthesia_type",
        schema = "public"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasAnaesthesiaType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anaesthesia_type_id")
    private Long anaesthesiaTypeId;

    @Column(name = "anaesthesia_type_code", nullable = false, length = 20, unique = true)
    private String anaesthesiaTypeCode;

    @Column(name = "anaesthesia_type_name", nullable = false,   unique = true)
    private String anaesthesiaTypeName;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by")
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}