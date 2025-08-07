package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingSampleResponse {
    private LocalDate reqDate;
    private String patientName;
    private String relation;
    private String name;
    private String age;
    private String gender;
    private String mobile;
    private String department;
    private String doctorName;
    private String priority;
    private String investigation;
    private String sample;
    private String collection;  // ✅ Changed from equipment



}
