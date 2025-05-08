package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "doctor_roaster")
public class DoctorRoaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private MasDepartment department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private User doctorId;

    @Column(name = "roaster_date")
    private Date roasterDate;

    @Size(max = 2)
    @Column(name = "roaster_value", length = 2)
    private String roasterValue;

    @Column(name = "chg_by")
    private Long chgBy;

    @Column(name = "chg_date")
    private LocalDate chgDate;

    @Size(max = 5)
    @Column(name = "chg_time", length = 5)
    private String chgTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospital;

}
