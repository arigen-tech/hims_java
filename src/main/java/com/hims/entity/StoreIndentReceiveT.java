package com.hims.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_indent_receive_t")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreIndentReceiveT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receive_t_id")
    private Long receiveTId;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "brand_name", length = 200)
    private String brandName;

    @Column(name = "manufacturer_name", length = 200)
    private String manufacturerName;

    @Column(name = "issued_qty", precision = 10, scale = 2)
    private BigDecimal issuedQty;

    @Column(name = "received_qty", precision = 10, scale = 2)
    private BigDecimal receivedQty;

    @Column(name = "rejected_qty", precision = 10, scale = 2)
    private BigDecimal rejectedQty;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @ManyToOne
    @JoinColumn(name = "indent_t_id", referencedColumnName = "indent_t_id")
    private StoreInternalIndentT storeInternalIndentT;

    @ManyToOne
    @JoinColumn(name = "store_issue_t_id", referencedColumnName = "store_issue_t_id")
    private StoreIssueT storeIssueT;

    @ManyToOne
    @JoinColumn(name = "receive_m_id", referencedColumnName = "receive_m_id")
    private StoreIndentReceiveM storeIndentReceiveM;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", nullable = false)
    private MasStoreItem item;

}
