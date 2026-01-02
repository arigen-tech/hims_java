package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "dob")
    private LocalDate dob;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gender_id")
    private MasGender genderId;

    @Column(name = "address_1", length = 255)
    private String address1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private MasCountry countryId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "state_id")
    private MasState stateId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "district_id")
    private MasDistrict districtId;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "mobile_no", length = 15)
    private String mobileNo;

    @Column(name = "registration_no", length = 100)
    private String registrationNo;

    @ManyToOne
    @JoinColumn(name = "employment_type_id", referencedColumnName = "id")
    private MasEmploymentType employmentTypeId;

    @Column(name = "profile_pic_name", length = 200)
    private String profilePicName;

    @Column(name = "id_document_name", length = 200)
    private String idDocumentName;

//    @ManyToOne
//    @JoinColumn(name = "department_id", referencedColumnName = "department_id")
//    private MasDepartment departmentId;

    @Column(name = "from_dt")
    private Instant fromDate;

    @ManyToOne
    @JoinColumn(name = "identification_type_id", referencedColumnName = "identification_type_id")
    private MasIdentificationType identificationType;

    @ManyToOne
    @JoinColumn(name = "employee_type_id", referencedColumnName = "user_type_id")
    private MasUserType employeeTypeId;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private MasRole roleId;

    @Column(name = "age")
    private Integer age;

    @Column(name = "email_id", length = 100)
    private String email;

    @Column(name = "address_2", length = 255)
    private String address2;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @Column(name = "approved_dt")
    private LocalDateTime approvedDate;

    @Column(name = "last_chg_by", length = 200)
    private String lastChangedBy;

    @Column(name = "last_chg_dt", columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    private Instant lastChangedDate;

    @Column(name = "year_of_exp")
    private Integer yearOfExperience;

    @OneToOne
    @JoinColumn(name = "designation_id", referencedColumnName = "designation_id")
    private MasDesignation masDesignationId;

    public Long getDesignationId() {
        return masDesignationId != null ? masDesignationId.getDesignationId() : null;
    }

    @Column(name = "profile_description", columnDefinition = "TEXT")
    private String profileDescription;
}
