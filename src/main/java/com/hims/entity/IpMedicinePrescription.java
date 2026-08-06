package com.hims.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_medicine_prescription", schema = "public")
public class IpMedicinePrescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private MasWard ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MasStoreItem item;

    @Column(name = "item_name", length = 300)
    private String itemName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_class_id")
    private MasItemClass itemClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private MasRoute route;

    @Column(name = "dose", length = 100)
    private String dose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frequency_id")
    private MasFrequency frequency;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "stop_date")
    private LocalDateTime stopDate;

    @Column(name = "stop_reason", length = 255)
    private String stopReason;

    @Column(name = "order_reference", length = 100)
    private String orderReference;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "administrated_by", length = 200)
    private String administratedBy;

    @Column(name = "total_days")
    private Long totalDays;


}
