package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
//
@Getter
@Setter
@Entity
@Table(
        name = "transaction_sequence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transaction_sequence",
                        columnNames = {
                                "transaction_name",
                                "hospital_id",
                                "financial_year"
                        }
                )
        }
)
public class TransactionSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_sequence_id")
    private Long transactionSequenceId;

    @Column(name = "transaction_name", nullable = false, length = 50)
    private String transactionName;

    @Column(name = "transaction_prefix", nullable = false, length = 10)
    private String transactionPrefix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private MasHospital hospital;

    @Column(name = "financial_year", nullable = false, length = 5)
    private String financialYear;

    @Column(name = "current_sequence", nullable = false)
    private Long currentSequence = 0L;

    @Column(name = "status", length = 1)
    private String status = "Y";

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "last_chg_by")
    private Long lastChgBy;
}