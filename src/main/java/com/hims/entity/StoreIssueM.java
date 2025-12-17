package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_issue_m")
@Data
public class StoreIssueM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_issue_m_id")
    private Long storeIssueMId;

    @Column(name = "issue_no", length = 100)
    private String issueNo;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_store_id")
    private MasDepartment fromStoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_dept_id")
    private MasDepartment toDeptId;

    @Column(name = "patient_id")
    private Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospitalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_m_id")
    private StoreInternalIndentM indentMId;

    @Column(name = "prescription_hd_id")
    private Long prescriptionHdId;

    @Column(name = "total_qty", precision = 14, scale = 2)
    private BigDecimal totalQty;

    @Column(name = "total_cost", precision = 14, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "total_discount", precision = 14, scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "total_gst", precision = 14, scale = 2)
    private BigDecimal totalGst;

    @Column(name = "net_amount", precision = 14, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "status", length = 3)
    private String status;

    @Column(name = "issued_by", length = 200)
    private String issuedBy;

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;
}
