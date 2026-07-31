package com.hims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pacs_hmis_study")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PacsHmisStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_no", length = 50, nullable = false)
    private String orderNo;

    @Column(name = "uhid", length = 50, nullable = false)
    private String uhid;

    @Column(name = "study_instance_uid", length = 128, nullable = false)
    private String studyInstanceUid;

    @Column(name = "modality", length = 10, nullable = false)
    private String modality;

    @Column(name = "study_description", length = 255)
    private String studyDescription;

    @Column(name = "study_datetime", nullable = false)
    private LocalDateTime studyDatetime;

    @Column(name = "study_status", length = 20, nullable = false)
    private String studyStatus;

    @Column(name = "pacs_source", length = 50)
    private String pacsSource;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
