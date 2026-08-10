package com.hims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "opd_refund_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpdRefundDetails {

    @Id
    @Column(name = "opd_refund_id", nullable = false)
    private Long opdRefundId;

    @Column(name = "billing_hd_id")
    private Long billingHdId;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(
            name = "refund_amout",
            precision = 10,
            scale = 2
    )
    private BigDecimal refundAmount;

    @Column(name = "txn_no")
    private String transactionNumber;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "refund_mode")
    private String refundMode;

    @Column(name = "processed_by")
    private String processedBy;
}