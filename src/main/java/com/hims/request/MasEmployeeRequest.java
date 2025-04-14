package com.hims.request;

import com.hims.entity.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private MultipartFile idDocumentName;
    private MultipartFile  profilePicName;


    private List<EmployeeQualificationReq> qualification;
    private List<EmployeeDocumentReq> document;
}
