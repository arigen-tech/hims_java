package com.hims.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_document", schema = "public")
public class IpDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ip_document_id", nullable = false)
    private Long ipDocumentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "document_datetime")
    private LocalDateTime documentDatetime = LocalDateTime.now();

    @Column(name = "document_type",length = 100)
    private String documentType;

    @Column(name = "document_notes", length = 500)
    private String documentNotes;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "file_size_kb")
    private Long fileSizeKb;

    @Column(name = "upload_source", length = 50)
    private String uploadSource;

    @Column(name = "uploaded_by", length = 150)
    private String uploadedBy;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate = LocalDateTime.now();


}