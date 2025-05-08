package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "dg_orderhd")
public class DgOrderHd {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderhd_id", nullable = false)
    private int id;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "order_no", length = 50)
    private String orderNo;

    @Column(name = "bar_code", length = 100)
    private String barCode;

    @Column(name = "order_status", length = 1)
    private String orderStatus;

    @Column(name = "collection_status", length =1)
    private String collectionStatus;

    @Column(name = "payment_status", length =1)
    private String paymentStatus;

    @Column(name = "createdby", length = 12)
    private String createdBy;

    @Column(name = "createdon")
    private LocalDate createdOn;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "last_chg_time",length = 10)
    private String lastChgTime;

    @Column(name = "lab_order_status",length = 4)
    private String labOrderStatus ;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate ;

    @Column(name = "other_investigation",length = 500)
    private String otherInvestigation;

    @Column(name = "hospital_id")
    private int hospitalId ;

    @Column(name = "prescribed_by")
    private int prescribedBy ;

    @Column(name = "department_id")
    private int departmentId ;

    @Column(name = "investigation_request_no")
    private int investigationRequestNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "visit_id")
    private Visit visitId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    private Patient patientId;

    @Column(name = "discount_id")
    private int discountId;

    @Column(name = "last_chg_by", length = 50)
    private String lastChgBy;

}
