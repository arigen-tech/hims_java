package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "mas_payment_mode",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "mas_payment_mode_mode_code_key", columnNames = "mode_code")
        }
)
public class MasPaymentMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_mode_id", nullable = false)
    private Long paymentModeId;

    @Column(name = "mode_code", nullable = false, length = 50, unique = true)
    private String modeCode;

    @Column(name = "mode_name", nullable = false, length = 100)
    private String modeName;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "status", length = 1)
    private String status;

}