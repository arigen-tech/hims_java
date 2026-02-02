package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_employee_language_mapping")
@Data
@Getter
@Setter
public class MasEmployeeLanguageMapping {

    @EmbeddedId
    private EmployeeLanguageId id;

    @ManyToOne
    @MapsId("empId")
    @JoinColumn(name = "emp_id")
    private MasEmployee employee;

    @ManyToOne
    @MapsId("languageId")
    @JoinColumn(name = "language_id")
    private MasLanguage language;

    @Column(name = "last_chg_by")
    private Long lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;
}
