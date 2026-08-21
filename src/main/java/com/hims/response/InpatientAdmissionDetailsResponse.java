package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InpatientAdmissionDetailsResponse {
    //Patient Information
    private Long patientId;
    private String patientName;
    private String uhid;
    private String age;
    private Long genderId;
    private String gender;
    private String contactNo;
    private String emergencyContactNo;

    //Admission Information
    private String admissionNo;
    private LocalDate admissionDate;
    private LocalTime admissionTime;
    private String admissionCategory;
    private String admissionType;
    private String admissionSource;
    private String currentStatus;
    private String los;

    //Doctor & Location
    private String admittingDoctor;
    private String department;
    private String admittingWard;
    private String currentWard;
    private String room;
    private String bed;
    private String careLevel;

    //   //Doctor & Location
    private String reasonForAdmission;
    private String initialDiagnosis;
    private String icdDiagnosis;
    private String patientCondition;
    private String admissionPriority;
    private String remark;

    //NOK Details
    private String nokName;
    private String relationship;
    private String contact;
    private String address;

    //Document Details
    List<DocumentList> documentListList;

   @Data
   @Builder
    public static class DocumentList {
       private String documentName;
    private String documentRemarks;
    private String fileName;
    private String filePath;

}
}
