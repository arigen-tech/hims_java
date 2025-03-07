package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_group")
@Getter
@Setter
public class MasBloodGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_group_id")
    private Long bloodGroupId;

    @Column(name = "blood_group_code", length = 8)
    private String bloodGroupCode;

    @Column(name = "blood_group_name", length = 30)
    private String bloodGroupName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 12)
    private String lastChangedBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChangedDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChangedTime;

    @Column(name = "hic_code", length = 25)
    private String hicCode;
}
