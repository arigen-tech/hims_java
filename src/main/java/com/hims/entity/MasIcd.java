package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mas_icd")
public class MasIcd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "icd_id")
    private Long icdId;

    @Column(name = "icd_code", length = 8)
    private String icdCode;

    @Column(name = "icd_name", length = 150)
    private String icdName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @Column(name = "icd_cause_id")
    private Integer icdCauseId;

    @Column(name = "icd_sub_category_id")
    private Integer icdSubCategoryId;
}
