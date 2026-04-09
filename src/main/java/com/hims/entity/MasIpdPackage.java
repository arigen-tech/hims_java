package com.hims.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_ipd_package")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasIpdPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "package_name", length = 200)
    private String packageName;

    @Column(name = "package_code", length = 50)
    private String packageCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_type_id")
    private MasAdmissionCategory packageTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private MasDepartment deptId;

    @Column(name = "stay_days")
    private Integer stayDays;

    @Column(name = "min_stay_days")
    private Integer minStayDays;

    @Column(name = "max_stay_days")
    private Integer maxStayDays;

    @Column(name = "description")
    private String description;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "generated_inclusions")
    private String generatedInclusions;

    @Column(name = "generated_exclusions")
    private String generatedExclusions;

}