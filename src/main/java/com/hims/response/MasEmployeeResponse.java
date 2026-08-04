package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
@Data
public class MasEmployeeResponse {
  private   Long employeeId;
    private  String firstName;
    private String middleName;
    private  String lastName;
    private  LocalDate dob;
    private  Long genderId;
    private  String gender;
    private  String mobileNo;
    private  Long employmentTypeId;
    private  String employmentType;
    private  Long employeeTypeId;
    private  String employeeType;
    private  String status;
    private Long roleId;
    private String roleName;

}
