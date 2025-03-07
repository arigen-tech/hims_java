package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_qualification")
@Getter
@Setter
public class EmployeeQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_qualification_id")
    private Long employeeQualificationId;

    @Column(name = "institution_name", length = 500)
    private String institutionName;

    @Column(name = "completion_year")
    private Integer completionYear;

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

    @Column(name = "qualification_name", length = 200)
    private String qualificationName;

    @Column(name = "file_path", length = 500)
    private String filePath;
}
