package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class MandatoryTestingSaveRequest {

    private Long donationId;
    private List<TestResultRequest> testResults;
    private List<String> filepath;

}