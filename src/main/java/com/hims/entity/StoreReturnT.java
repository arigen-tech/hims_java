package com.hims.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "store_return_t")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreReturnT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_t_id")
    private Long returnTId;



    @Column(name = "batch_no", length = 120)
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "dom")
    private LocalDate dom;  // Date of Manufacture

    @Column(name = "brand_name", length = 200)
    private String brandName;

    @Column(name = "manufacturer_name", length = 200)
    private String manufacturerName;




    @Column(name = "rejected_qty", precision = 10, scale = 2)
    private BigDecimal rejectedQty;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "usable_qty")
    private BigDecimal usableQty;

    @Column(name = "damaged_qty")
    private BigDecimal damagedQty;

    @Column(name = "last_update_date")
    private java.time.LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "store_verification", length = 200)
    private String storeVerification;


    @ManyToOne
    @JoinColumn(name = "return_m_id", referencedColumnName = "return_m_id")
    private StoreReturnM storeReturnM;

    @ManyToOne
    @JoinColumn(name = "store_issue_t_id", referencedColumnName = "store_issue_t_id")
    private StoreIssueT storeIssueT;

    @ManyToOne
    @JoinColumn(name = "receive_t_id", referencedColumnName = "receive_t_id")
    private StoreIndentReceiveT storeIndentReceiveT;

    @ManyToOne
    @JoinColumn(name = "stock_id", referencedColumnName = "stock_id")
    private StoreItemBatchStock storeItemBatchStock;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private MasStoreItem masStoreItem;
}
