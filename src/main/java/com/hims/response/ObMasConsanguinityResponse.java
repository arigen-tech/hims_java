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
public class ObMasConsanguinityResponse {

    private Long id;
    private String consanguinityValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
