
package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ot_pac_document",
        schema = "public"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtPacDocument {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "pac_document_id")
 private Long pacDocumentId;

 @JsonIgnore
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "pac_assessment_id", nullable = false)
 private OtPacAssessment pacAssessment;

 @Column(name = "document_name", nullable = false, length = 255)
 private String documentName;

 @Column(name = "file_name", nullable = false, length = 255)
 private String fileName;

 @Column(name = "file_path", nullable = false, length = 1000)
 private String filePath;

 @Column(name = "mime_type", length = 100)
 private String mimeType;

 @Column(name = "file_size")
 private Long fileSize;

 @Column(name = "uploaded_by", length = 200)
 private String uploadedBy;

 @Column(name = "uploaded_date", nullable = false)
 private LocalDateTime uploadedDate;

 @Column(name = "status", nullable = false, length = 1)
 private String status;

 @Column(name = "last_chg_by", length = 200)
 private String lastChgBy;

 @Column(name = "last_chg_date", nullable = false)
 private LocalDateTime lastChgDate;
}
