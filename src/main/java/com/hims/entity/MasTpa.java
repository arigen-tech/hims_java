package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mas_tpa", schema = "public")
public class MasTpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tpa_id")
    private Long tpaId;

    @Column(name = "tpa_name", length = 200)
    private String tpaName;

    @Column(name = "tpa_code", length = 50)
    private String tpaCode;

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
