package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "mas_employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private Long employeeId;

    @Column(name = "emp_fn", length = 100, nullable = false)
    private String firstName;

    @Column(name = "emp_mn", length = 100)
    private String middleName;

    @Column(name = "emp_ln", length = 100, nullable = false)
    private String lastName;

    @Column(name = "gender_id")
    private Integer genderId;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "age")
    private Integer age;

    @Column(name = "mobile_no", length = 15)
    private String mobileNo;

    @Column(name = "email_id", length = 100)
    private String email;

    @Column(name = "address_1", length = 255)
    private String address1;

    @Column(name = "address_2", length = 255)
    private String address2;

    @Column(name = "country_id")
    private Integer countryId;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "registration_no", length = 100)
    private String registrationNo;

    @Column(name = "employee_type_id")
    private Integer employeeTypeId;

    @Column(name = "employment_type_id")
    private Integer employmentTypeId;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChangedBy;

    @Column(name = "last_chg_dt", columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    private Instant lastChangedDate;
}
