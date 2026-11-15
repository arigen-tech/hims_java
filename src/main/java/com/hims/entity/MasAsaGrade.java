package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_asa_grade", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasAsaGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "grade_code", nullable = false, length = 5, unique = true)
    private String gradeCode;

    @Column(name = "grade_name", nullable = false, length = 20)
    private String gradeName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_update_dt")
    private LocalDateTime lastUpdateDt;
}
