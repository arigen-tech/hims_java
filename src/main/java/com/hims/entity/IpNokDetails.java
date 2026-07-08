package com.hims.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_nok_details", schema = "public")
public class IpNokDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nok_id", nullable = false)
    private Long nokId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ip_admission_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "nok_name" ,length = 100)
    private String nokName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nok_relation_id")
    private MasRelation nokRelation;

    @Column(name = "contact_no", length = 20)
    private String contactNo;

    @Column(name = "address_line", length = 250)
    private String addressLine;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "is_primary", length = 1)
    private String isPrimary;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate = LocalDateTime.now();

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;


}