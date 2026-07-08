package com.hims.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasAdmissionSourceRequest {

    private String admissionSourceName;

    private String description;


}