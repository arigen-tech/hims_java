package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "dg_orderdt")
public class DgOrderDt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderdt_id", nullable = false)
    private int id;

    @Column(name = "charge_cost")
    private Double chargeCost;

    @Column(name = "discount_amt", length = 8)
    private Double discountAmt;

    @Column(name = "order_qty", length = 8)
    private int orderQty;

    @Column(name = "order_status", length = 1)
    private String orderStatus;

    @Column(name = "createdby", length = 100)
    private String createdBy;

//    @Column(name = "create_don")
//    private LocalDate createdOn;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "last_chg_time", length = 64)
    private String lastChgTime;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigationId;

    @Column(name = "sub_chargeid")
    private long subChargeid;

    @Column(name = "main_chargecode_id")
    private long mainChargecodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderhd_id")
    private DgOrderHd orderhdId;

    @Column(name = "billing_status", length = 1)
    private String billingStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private DgInvestigationPackage packageId;

    @Column(name = "createdon")
    private Instant createdon;

    @Size(max = 1)
    @Column(name = "msg_sent", length = 1)
    private String msgSent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_hd_id")
    private BillingHeader billingHd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_tracking_status_id")
    private LabOrderTrackingStatus orderTrackingStatus;

}
