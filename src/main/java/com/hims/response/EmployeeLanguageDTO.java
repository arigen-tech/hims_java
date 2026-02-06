package com.hims.response;

import com.hims.entity.MasEmployeeLanguageMapping;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EmployeeLanguageDTO {
    private Long languageId;
    private String languageName;

    public static EmployeeLanguageDTO fromEntity(MasEmployeeLanguageMapping mapping) {
        return EmployeeLanguageDTO.builder()
                .languageId(mapping.getLanguage().getLanguageId())
                .languageName(mapping.getLanguage().getLanguageName())
                .build();
    }
}