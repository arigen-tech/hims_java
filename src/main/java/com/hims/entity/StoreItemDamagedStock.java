package com.hims.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_item_damaged_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemDamagedStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "damaged_id")
    private Long damagedId;

//    @Column(name = "stock_id", nullable = false)
//    private Long stockId;
//
//    @Column(name = "item_id", nullable = false)
//    private Long itemId;
//
//    @Column(name = "source_department_id", nullable = false)
//    private Long sourceDepartmentId;
//
//    @Column(name = "return_m_id")
//    private Long returnMId;
//
//    @Column(name = "return_t_id")
//    private Long returnTId;

    @Column(name = "damaged_qty", precision = 10, scale = 2)
    private BigDecimal damagedQty;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "reported_date", nullable = false)
    private LocalDateTime reportedDate;

    @Column(name = "recorded_by", length = 200)
    private String recordedBy;

    @Column(name = "brand_name", length = 200)
    private String brandName;

    @Column(name = "manufacturer_name", length = 200)
    private String manufacturerName;

    @Column(name = "disposal_status", length = 20)
    private String disposalStatus;

    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "disposal_date")
    private LocalDateTime disposalDate;

    @Column(name = "disposal_method", length = 200)
    private String disposalMethod;

    @Column(name = "disposal_witness", length = 200)
    private String disposalWitness;

    @Column(name = "disposal_remark", length = 300)
    private String disposalRemark;

    @Column(name = "committee_approval", length = 1000)
    private String committeeApproval;

    @Column(name = "committee_approval_date")
    private LocalDate committeeApprovalDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;


    @ManyToOne
    @JoinColumn(name = "source_department_id", referencedColumnName = "department_id")
    private MasDepartment sourceDepartment;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private MasStoreItem masStoreItem;

    @ManyToOne
    @JoinColumn(name = "stock_id", referencedColumnName = "stock_id")
    private StoreItemBatchStock storeItemBatchStock;

    @ManyToOne
    @JoinColumn(name = "return_m_id", referencedColumnName = "return_m_id")
    private StoreReturnM storeReturnM;

    @ManyToOne
    @JoinColumn(name = "return_t_id", referencedColumnName = "return_t_id")
    private StoreReturnT storeReturnT;
}
