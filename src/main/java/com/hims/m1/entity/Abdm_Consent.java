package com.hims.m1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ABDM_CONSENT")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Abdm_Consent implements Serializable {

    @Id
    @Column(name = "ABDM_CONSENT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "abdm_consent_seq")
    @SequenceGenerator(
            name = "abdm_consent_seq",
            sequenceName = "ABDM_CONSENT_SEQ",
            allocationSize = 1
    )
    private Long abdmApiLogId;

    @Column(name = "CONSENT_NAME")
    private String consentName;

    @Column(name = "AADHAR_NUMBER")
    private String aadhaarNumber;

    @Column(name = "CONSENT_1")
    private String consent1;

    @Column(name = "CONSENT_2")
    private String consent2;

    @Column(name = "CONSENT_3")
    private String consent3;

    @JsonIgnore
    @Column(name = "CONSENT_4")
    private String consent4;

    @Column(name = "CONSENT_5")
    private String consent5;

    @Column(name = "CONSENT_6")
    private String consent6;

    @Column(name = "CONSENT_7")
    private String consent7;

    @JsonIgnore
    @Column(name = "HOSPITAL_ID")
    private Long hospitalId;

    @JsonIgnore
    @Column(name = "CREATED_BY")
    private Long createdBy;

    @JsonIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate = LocalDateTime.now();

    @JsonIgnore
    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @Column(name = "IS_ACTIVE")
    private Long isActive = 1L;

}
