package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mas_receipt_type", uniqueConstraints = {
                @UniqueConstraint(name = "uk_receipt_type_code", columnNames = "receipt_type_code")
        }
)
public class MasReceiptType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_type_id")
    private Long receiptTypeId;

    @Column(name = "receipt_type_code", nullable = false, length = 20)
    private String receiptTypeCode;

    @Column(name = "receipt_type_name", nullable = false, length = 100)
    private String receiptTypeName;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}