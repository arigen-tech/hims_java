package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opd_holiday_master", schema = "public")
@Data
public class OpdHolidayMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opd_holiday_id")
    private Long opdHolidayId;

    @Column(name = "holiday_date")
    private LocalDate holidayDate;

    @Column(name = "holiday_name", length = 100)
    private String holidayName;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 250)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 250)
    private String updatedBy;

    @Column(name = "last_updated_dt")
    private LocalDateTime lastUpdatedDt;
}
