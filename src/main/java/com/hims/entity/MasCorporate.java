package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mas_corporate", schema = "public")
public class MasCorporate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "corporate_id")
    private Long corporateId;

    @Column(name = "corporate_name", length = 200)
    private String corporateName;

    @Column(name = "corporate_code", length = 50)
    private String corporateCode;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "contact_no", length = 50)
    private String contactNo;

    @Column(name = "email_id", length = 200)
    private String emailId;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "credit_allowed", length = 1)
    private String creditAllowed;

    @Column(name = "credit_days")
    private Integer creditDays;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "status", length = 1)
    private String status;


}