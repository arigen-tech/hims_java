package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ot_booking_request_dt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtBookingRequestDt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_booking_request_dt_id")
    private Long otBookingRequestDtId;

    @ManyToOne
    @JoinColumn(name = "ot_booking_request_id", referencedColumnName = "ot_booking_request_id")
    private OtBookingRequestHd otBookingRequest;

    @Column(name = "surgery_type_id")
    private Long surgeryTypeId;

    @Column(name = "surgery_id")
    private Long surgeryId;

    @Column(name = "sequence_no")
    private Long sequenceNo;

    @Column(name = "expected_duration_min")
    private Integer expectedDurationMin;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

}
