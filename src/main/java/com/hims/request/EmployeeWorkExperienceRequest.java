package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkExperienceRequest {
    private String experienceId;
    private String experienceSummary;
}
