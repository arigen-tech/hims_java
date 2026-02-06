package com.hims.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLanguageRequest {
    private long languageId;
   private String languageName;;
}
