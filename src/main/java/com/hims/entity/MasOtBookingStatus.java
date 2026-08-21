package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_ot_booking_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasOtBookingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_status_id", nullable = false)
    private Long bookingStatusId;

    @Column(name = "status_code", length = 30, nullable = false)
    private String statusCode;

    @Column(name = "status_name", length = 100, nullable = false)
    private String statusName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", columnDefinition = "bpchar(1)", nullable = false)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChangedBy;

    @Column(name = "last_chg_date", nullable = false, updatable = false)
    private LocalDateTime lastChangedDate;

}