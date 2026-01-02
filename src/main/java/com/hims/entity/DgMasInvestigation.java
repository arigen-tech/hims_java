package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name="dg_mas_investigation")
public class DgMasInvestigation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investigation_id", nullable = false)
    private Long investigationId;
    @Column(name = "investigation_name", length =200)
    private String investigationName;
    @Column(name = "status", length=1)
    private String status;
    @Column(name = "confidential", length =1)
    private String confidential;
    @Column(name = "appear_in_discharge_summary", length =1)
    private String appearInDischargeSummary;
    @Column(name = "investigation_type", length =1)
    private String investigationType;
    @Column(name = "multiple_results", length =1)
    private String multipleResults;
    @Column(name = "quantity", length =10)
    private String quantity;
    @Column(name = "normal_value", length =20)
    private String normalValue;
    @Column(name = "last_chg_by", length =12)
    private String lastChgBy;
    @Column(name = "last_chg_date")
    private Instant lastChgDate;
    @Column(name = "last_chg_time",length =10)
    private String lastChgTime;
    @Column(name = "appointment_required", length =10)
    private String appointmentRequired;
    @Column(name = "max_normal_value", length =10)
    private String maxNormalValue;
    @Column(name = "min_normal_value", length=10)
    private String minNormalValue;
    @Column(name = "test_order_no" )
    private Long testOrderNo;
    @Column(name = "numeric_or_string", length =1)
    private String numericOrString;
    @Column(name = "hic_code",length=25)
    private String hicCode;
//    @Column(name = "charge_code_id")
//    private MasChargeCode chargeCodeId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "main_chargecode_id")
    private MasMainChargeCode mainChargeCodeId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uom_id")
    private DgUom uomId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_chargecode_id")
    private MasSubChargeCode subChargeCodeId ;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_id")
    private DgMasSample sampleId;
    @Column(name = "equipment_id")
    private String equipmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collection_id")
    private DgMasCollection collectionId;

    @Column(name = "blood_reaction_test", length =1)
    private String bloodReactionTest;
    @Column(name = "blood_bank_screen_test",length =1)
    private String bloodBankScreenTest;
    @Column(name = "instructions", length=1500)
    private String instructions;
    @Column(name = "discount_applicable", length=1)
    private String discountApplicable;
    @Column(name = "gender_applicable",length=1)
    private String genderApplicable;
    @Column(name = "discount", length=255)
    private String discount;
    @Column(name = "price", length=255)
    private Double price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MasInvestigationCategory categoryId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "method_id")
    private MasInvestigationMethodology methodId;

    @Column(name = "interpretation",length = 500)
    private String interpretation;

    @Column(name = "preparation_text",columnDefinition = "text")
    private String preparationText;

    @Column(name = "tat_hours")
    private Integer tatHours;

    @Column(name = "estimated_days")
    private Integer estimatedDays;
}
