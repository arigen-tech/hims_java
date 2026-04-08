package com.hims.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mas_visit_type", schema = "public")
public class MasVisitType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_type_id")
    private Long visitTypeId;

    @Column(name = "visit_type_code", length = 50)
    private String visitTypeCode;

    @Column(name = "visit_type_name", length = 100)
    private String visitTypeName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 100)
    private String lastChangedBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChangedDate;


}