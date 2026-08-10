package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_procedure_txn", schema = "public")
public class IpProcedureTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_txn_id", nullable = false)
    private Long procedureTxnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @Column(name = "procedure_id", nullable = false)
    private Long procedureId;

    @Column(name = "procedure_name", length = 200)
    private String procedureName;

    @Column(name = "procedure_datetime", nullable = false)
    private LocalDateTime procedureDatetime;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "is_billed")
    private Boolean isBilled = false;

    @Column(name = "bill_item_id")
    private Long billItemId;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

   
}