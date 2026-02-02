package com.hims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class EmployeeLanguageId implements Serializable {

    @Column(name = "emp_id")
    private Long empId;

    @Column(name = "language_id")
    private Long languageId;

}
