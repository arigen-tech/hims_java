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
public class ObMasPresentationResponse {

    private Long id;
    private String presentationValue;
    private String status;
    private LocalDateTime lastUpdateDate;
}
