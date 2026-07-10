package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_insurance", schema = "public")
@Data
public class MasInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "insurance_id")
    private Long insuranceId;

    @Column(name = "insurance_name", length = 200)
    private String insuranceName;

    @Column(name = "insurance_code", length = 50)
    private String insuranceCode;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "contact_no", length = 50)
    private String contactNo;

    @Column(name = "email_id", length = 200)
    private String emailId;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "status", length = 1)
    private String status;

}