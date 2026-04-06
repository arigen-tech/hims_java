package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_donation_investigation_doc", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonationInvestigationDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long docId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    private BloodDonationHdr donation;


    @Column(name = "doc_type", length = 50)
    private String docType;


    @Column(name = "file_name", length = 200)
    private String fileName;


    @Column(name = "file_path", length = 500)
    private String filePath;


    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;


    @Column(name = "uploaded_by", length = 200)
    private String uploadedBy;
}