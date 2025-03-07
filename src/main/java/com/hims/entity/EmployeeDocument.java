package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_document")
@Getter
@Setter
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_document_id")
    private Long employeeDocumentId;

    @Column(name = "document_name", length = 200)
    private String documentName;

    @Column(name = "document_encypt_name", length = 500)
    private String documentEncryptName;

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "emp_id")
    private MasEmployee employee;

    @Column(name = "last_chg_by", length = 200)
    private String lastChangedBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChangedDate;

    @Column(name = "file_path", length = 300)
    private String filePath;
}
