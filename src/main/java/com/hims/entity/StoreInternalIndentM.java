package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_internal_indent_m")
@Data
public class StoreInternalIndentM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indent_m_id")
    private Long indentMId;

    @Column(name = "indent_no", length = 150)
    private String indentNo;

    @Column(name = "indent_date")
    private LocalDateTime indentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_dept_id")
    private MasDepartment fromDeptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_dept_id")
    private MasDepartment toDeptId;

    @Column(name = "total_cost", precision = 14, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "store_approved_by", length = 150)
    private String storeApprovedBy;

    @Column(name = "store_approved_date")
    private LocalDateTime storeApprovedDate;

    @Column(name = "issued_by", length = 50)
    private String issuedBy;

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    @Column(name = "received_by", length = 150)
    private String receivedBy;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "issue_no", length = 150)
    private String issueNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_issue_m_id")
    private StoreIssueM storeIssueMId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "remarks", length = 200)
    private String remarks;

}
