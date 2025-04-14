package com.hims.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class MasEmploymentTypeResponse {

    private Long id;
    private String employmentType;
    private String status;
    private String lastChangedBy;
    private LocalDateTime lastChangedDate;
}
