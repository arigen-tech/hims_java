package com.hims.entity;

import com.hims.entity.MasStoreItem;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "patient_prescription_dt", schema = "public")
public class PatientPrescriptionDt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_dt_id")
    private Long prescriptionDtId;

    @Column(name = "prescription_hd_id", nullable = false)
    private Long prescriptionHdId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "dosage", length = 50)
    private String dosage;

    @Column(name = "frequency", length = 50)
    private String frequency;

    @Column(name = "days")
    private Integer days;

    @Column(name = "total", precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "issued_qty", precision = 12, scale = 2)
    private BigDecimal issuedQty = BigDecimal.ZERO;

    @Column(name = "route", length = 100)
    private String route;

    @Column(name = "instruction", length = 200)
    private String instruction;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount", precision = 5, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "gst_rate", precision = 5, scale = 2)
    private BigDecimal gstRate = BigDecimal.ZERO;

    @Column(name = "line_cost", precision = 12, scale = 2)
    private BigDecimal lineCost;

    @Column(name = "substitute_item_id")
    private Long substituteItemId;

    @Column(name = "status", length = 1)
    private String status = "N";

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_hd_id", insertable = false, updatable = false)
    private PatientPrescriptionHd prescriptionHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private MasStoreItem storeItem;
}
