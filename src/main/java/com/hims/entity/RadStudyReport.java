package com.hims.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rad_study_report", schema = "public")
public class RadStudyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rad_study_report_id", nullable = false)
    private Long radStudyReportId;



    @NotNull(message = "radOrderDt is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rad_orderdt_id", nullable = false)
    private RadOrderDt radOrderDt;


    @NotBlank(message = "reportDesc is required")
    @Column(name = "report_desc", columnDefinition = "text", nullable = false)
    private String reportDesc;

    @Size(max = 500, message = "reportImagePath max length is 500")
    @Column(name = "report_image_path", length = 500)
    private String reportImagePath;

    @NotBlank(message = "reportStatus is required")
    @Size(max = 1, message = "reportStatus must be 1 character")
    @Column(name = "report_status", length = 1)
    private String reportStatus;

    @NotBlank(message = "createdBy is required")
    @Size(max = 200, message = "createdBy max length is 200")
    @Column(name = "createdby", length = 200, nullable = false)
    private String createdBy;

    @NotNull(message = "createdOn is required")
    @Column(name = "createdon", nullable = false)
    private LocalDateTime createdOn;

    @Size(max = 200, message = "lastChgBy max length is 200")
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @PastOrPresent(message = "lastChgDate cannot be in the future")
    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;


}