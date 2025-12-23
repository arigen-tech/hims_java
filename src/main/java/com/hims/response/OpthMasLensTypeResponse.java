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
public class OpthMasLensTypeResponse {

    private Long id;
    private String lensType;
    private String status;
    private LocalDateTime lastUpdateDate;
}
