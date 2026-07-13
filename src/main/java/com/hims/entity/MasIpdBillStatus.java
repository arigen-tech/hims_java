package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DynamicInsert
@DynamicUpdate
@Table(
        name = "mas_ipd_bill_status",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "mas_ipd_bill_status_status_code_key", columnNames = "status_code")
        }
)
public class MasIpdBillStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_status_id", nullable = false)
    private Long billStatusId;

    @Column(name = "status_code", nullable = false, length = 50, unique = true)
    private String statusCode;

    @Column(name = "status_name", nullable = false, length = 100)
    private String statusName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "status", length = 1)
    private String status;
}