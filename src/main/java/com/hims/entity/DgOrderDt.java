package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
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

    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigationId;

    @Column(name = "sub_chargeid")
    private int subChargeid;

    @Column(name = "main_chargecode_id")
    private int mainChargecodeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "orderhd_id")
    private DgOrderHd orderhdId;

    @Column(name = "billing_status", length = 1)
    private String billingStatus;

    @Column(name = " msg_sent", length = 1)
    private String msgSent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "package_id")
    private DgInvestigationPackage packageId;
}
