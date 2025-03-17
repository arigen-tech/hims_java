package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mas_hospital")
public class MasHospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id", nullable = false)
    private Long id;

    @Size(max = 8)
    @Column(name = "hospital_code", length = 8)
    private String hospitalCode;

    @Size(max = 30)
    @Column(name = "hospital_name", length = 30)
    private String hospitalName;

    @Size(max = 1)
    @NotNull
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Size(max = 50)
    @Column(name = "address", length = 50)
    private String address;

    @Size(max = 12)
    @Column(name = "contact_number", length = 12)
    private String contactNumber;

    @Size(max = 12)
    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 10)
    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private MasCountry country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private MasState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private MasDistrict district;

    @Size(max = 10)
    @Column(name = "pin_code", length = 10)
    private String pinCode;

    @Size(max = 1)
    @Column(name = "reg_cost_applicable", length = 1)
    private String regCostApplicable;

    @Size(max = 1)
    @Column(name = "app_cost_applicable", length = 1)
    private String appCostApplicable;

    @Size(max = 1)
    @Column(name = "pre_consultation_available", length = 1)
    private String preConsultationAvailable;

    @Size(max = 20)
    @Column(name = "gstn_no", length = 20)
    private String gstnNo;

    @Size(max = 20)
    @Column(name = "contact_number2", length = 20)
    private String contactNumber2;

    @Size(max = 30)
    @Column(name = "city", length = 30)
    private String city;

    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

}
