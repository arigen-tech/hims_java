package com.hims.response;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasAdmissionSourceResponse {

    private Long id;

    private String admissionSourceName;

    private String description;

    private String status;

    private String createdBy;

    private String lastUpdatedBy;
}