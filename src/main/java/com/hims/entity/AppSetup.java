package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "app_setup")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AppSetup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 8)
    @Column(name = "from_time", length = 8)
    private String fromTime;

    @Size(max = 8)
    @Column(name = "to_time", length = 8)
    private String toTime;

    @Column(name = "max_no_of_days")
    private Integer maxNoOfDays;

    @Column(name = "min_no_of_days")
    private Integer minNoOfDays;

    @Column(name = "last_chg_by")
    private Integer lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Size(max = 10)
    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @Size(max = 45)
    @Column(name = "days", length = 45)
    private String days;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private MasDepartment dept;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctorId;

    @Column(name = "total_token")
    private Integer totalToken;

    @Column(name = "total_interval")
    private Integer totalInterval;

    @Column(name = "start_token")
    private Integer startToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private MasOpdSession session;

    @Column(name = "total_online_token")
    private Integer totalOnlineToken;

    @Column(name = "time_taken")
    private Integer timeTaken;

    @Size(max = 5)
    @Column(name = "start_time", length = 5)
    private String startTime;

    @Size(max = 5)
    @Column(name = "end_time", length = 5)
    private String endTime;

    @Size(max = 200)
    @Column(name = "opd_location", length = 200)
    private String opdLocation;
}
