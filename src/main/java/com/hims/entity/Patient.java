package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id", nullable = false)
    private Long id;

    @Size(max = 50)
    @Column(name = "uhid_no", length = 50)
    private String uhidNo;

    @Size(max = 50)
    @Column(name = "p_fn", length = 50)
    private String patientFn;

    @Size(max = 50)
    @Column(name = "p_mn", length = 50)
    private String patientMn;

    @Size(max = 30)
    @Column(name = "p_ln", length = 30)
    private String patientLn;

    @Column(name = "p_dob")
    private LocalDate patientDob;

    @Size(max = 50)
    @Column(name = "p_age", length = 50)
    private String patientAge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_gender_id")
    private MasGender patientGender;

    @Size(max = 70)
    @Column(name = "p_email_id", length = 70)
    private String patientEmailId;

    @Size(max = 20)
    @Column(name = "p_mobile_number", length = 20)
    private String patientMobileNumber;

    @Size(max = 255)
    @Column(name = "patient_image")
    private String patientImage;

    @Size(max = 50)
    @Column(name = "file_name", length = 50)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_relation_id")
    private MasRelation patientRelation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_marital_status_id")
    private MasMaritalStatus patientMaritalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_religion_id")
    private MasReligion patientReligion;

    @Size(max = 500)
    @Column(name = "p_address1", length = 500)
    private String patientAddress1;

    @Size(max = 500)
    @Column(name = "p_address2", length = 500)
    private String patientAddress2;

    @Size(max = 100)
    @Column(name = "p_city", length = 100)
    private String patientCity;

    @Size(max = 10)
    @Column(name = "p_pincode", length = 10)
    private String patientPincode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_district_id")
    private MasDistrict patientDistrict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_state_id")
    private MasState patientState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_country_id")
    private MasCountry patientCountry;

    @Size(max = 8)
    @Column(name = "pincode", length = 8)
    private String pincode;

    @Size(max = 50)
    @Column(name = "emer_fn", length = 50)
    private String emerFn;

    @Size(max = 50)
    @Column(name = "emer_ln", length = 50)
    private String emerLn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emer_relation")
    private MasRelation emerRelation;

    @Size(max = 20)
    @Column(name = "emer_mobile", length = 20)
    private String emerMobile;

    @Size(max = 50)
    @Column(name = "nok_fn", length = 50)
    private String nokFn;

    @Size(max = 50)
    @Column(name = "nok_ln", length = 50)
    private String nokLn;

    @Size(max = 70)
    @Column(name = "nok_email", length = 70)
    private String nokEmail;

    @Size(max = 20)
    @Column(name = "nok_mobile_number", length = 20)
    private String nokMobileNumber;

    @Size(max = 500)
    @Column(name = "nok_address1", length = 500)
    private String nokAddress1;

    @Size(max = 500)
    @Column(name = "nok_address2", length = 500)
    private String nokAddress2;

    @Size(max = 100)
    @Column(name = "nok_city", length = 100)
    private String nokCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nok_district_id")
    private MasDistrict nokDistrict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nok_state_id")
    private MasState nokState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nok_country_id")
    private MasCountry nokCountry;

    @Size(max = 8)
    @Column(name = "nok_pincode", length = 8)
    private String nokPincode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nok_relation_id")
    private MasRelation nokRelation;

    @Size(max = 20)
    @Column(name = "patient_status", length = 20)
    private String patientStatus;

    @Column(name = "reg_date")
    private LocalDate regDate;

    @NotNull
    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @NotNull
    @Column(name = "updated_on", nullable = false)
    private Instant updatedOn;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_hospital_id")
    private MasHospital patientHospital;

}
