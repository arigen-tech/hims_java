package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_ot_booking_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasOtBookingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_status_id")
    private Long bookingStatusId;

    @Column(name = "status_code", nullable = false, unique = true, length = 30)
    private String statusCode;

    @Column(name = "status_name", nullable = false, length = 100)
    private String statusName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}