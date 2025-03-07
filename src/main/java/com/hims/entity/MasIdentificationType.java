package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_identification_type")
@Getter
@Setter
public class MasIdentificationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identification_type_id")
    private Long identificationTypeId;

    @Column(name = "identification_code", length = 10)
    private String identificationCode;

    @Column(name = "identification_name", length = 120)
    private String identificationName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by")
    private Long lastChangedBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChangedDate;

    @Column(name = "map_id")
    private Long mapId;
}
