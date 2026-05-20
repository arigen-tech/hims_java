package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OpdHolidayMasterRequest {


    private LocalDate holidayDate;

    @NotBlank(message = "Holiday name is required")
    @Size(max = 100)
    private String holidayName;

    @Size(max = 255)
    private String remarks;
}