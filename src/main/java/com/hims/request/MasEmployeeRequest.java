package com.hims.request;

import com.hims.entity.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasEmployeeRequest {

    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dob;
    private Long genderId;
    private String address1;
    private Long countryId;
    private Long stateId;
    private Long districtId;
    private Long departmentId;
    private String city;
    private String pincode;
    private String mobileNo;
    private String registrationNo;
    private Long identificationType;
    private Long employmentTypeId;
    private Long employeeTypeId;
    private Long roleId;
    private Instant fromDate;
    private Integer yearOfExperience;
    private Long masDesignationId;
    private String profileDescription;

    private MultipartFile idDocumentName;
    private MultipartFile  profilePicName;

    private List<EmployeeLanguageRequest> languages;
    private List<EmployeeQualificationReq> qualification;
    private List<EmployeeSpecialtyCenterRequest> specialtyCenter;
    private List<EmployeeWorkExperienceRequest> workExperiences;
    private List<EmployeeMembershipRequest> employeeMemberships;
    private List<EmployeeSpecialtyInterestRequest> employeeSpecialtyInterests;
    private List<EmployeeAwardRequest> employeeAwards;
    private List<EmployeeDocumentReq> document;
}
