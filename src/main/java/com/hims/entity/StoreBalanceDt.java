package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="store_balance_t")
@Data
public class StoreBalanceDt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_t_id")
    private Long balanceTId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "balance_m_id")
    private StoreBalanceHd balanceMId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private MasStoreItem itemId;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "qty")
    private Long qty;

    @Column(name = "units_per_pack")
    private Long unitsPerPack;

    @Column(name = "purchase_rate_per_unit", precision = 10, scale = 2)
    private BigDecimal purchaseRatePerUnit;

    @Column(name = "gst_percent", precision = 5, scale = 2)
    private BigDecimal gstPercent;

    @Column(name = "mrp_per_unit", precision = 10, scale = 2)
    private BigDecimal mrpPerUnit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hsn_code")
    private MasHSN hsnCode;

    @Column(name = "base_rate_per_unit",  precision = 10, scale = 2)
    private BigDecimal baseRatePerUnit;

    @Column(name = "gst_amount_per_unit", precision = 10, scale = 2)
    private BigDecimal gstAmountPerUnit;

    @Column(name = "total_purchase_cost", precision = 12, scale = 2)
    private BigDecimal totalPurchaseCost;

    @Column(name = "total_mrp_value", precision = 12, scale = 2)
    private BigDecimal totalMrpValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private MasBrand brandId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manufacturer_id")
    private MasManufacturer manufacturerId;


}
