package com.hims.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class MasEmploymentTypeRequest {
    private String employmentType;
    private String status;
    private String lastChangedBy;



}
