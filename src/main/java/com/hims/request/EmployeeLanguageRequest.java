package com.hims.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLanguageRequest {
    private Long languageId;
    private String languageName;
}
