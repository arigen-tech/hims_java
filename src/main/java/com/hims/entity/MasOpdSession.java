package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "mas_opd_session")
public class MasOpdSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @NotNull
    @Column(name = "from_time", nullable = false)
    private LocalTime fromTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Size(max = 50)
    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Size(max = 255)
    @Column(name = "las_chg_vy")
    private String lasChgVy;

    @Column(name = "last_chg_dt")
    private LocalDate lastChgDt;

}
