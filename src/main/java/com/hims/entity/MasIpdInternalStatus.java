package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_ipd_internal_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasIpdInternalStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ipd_internal_status_id")
    private Long ipdInternalStatusId;

    @Column(name = "status_code", length = 50)
    private String statusCode;

    @Column(name = "status_name", length = 100)
    private String statusName;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 200)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

}