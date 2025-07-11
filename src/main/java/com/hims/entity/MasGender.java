package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mas_gender")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MasGender {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 1)
    @NotNull
    @Column(name = "gender_code", nullable = false, length = 1)
    private String genderCode;

    @NotNull
    @Column(name = "gender_name", nullable = false, length = Integer.MAX_VALUE)
    private String genderName;

    @Column(name = "last_chg_dt")
    private LocalDateTime lastChgDt;

    @Size(max = 1)
    @NotNull
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Size(max = 5)
    @Column(name = "code", length = 5)
    private String code;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

}
