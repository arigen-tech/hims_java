package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObMasImmunisedStatusResponse {

    private Long id;
    private String immunisationValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
