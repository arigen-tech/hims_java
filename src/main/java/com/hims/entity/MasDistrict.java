package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "mas_district")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MasDistrict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "district_name", nullable = false, length = 100)
    private String districtName;

    @Size(max = 1)
    @NotNull
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Size(max = 200)
    @Column(name = "las_ch_by", length = 200)
    private String lasChBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "state_id")
    private MasState state;

}
