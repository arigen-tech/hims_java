package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DentalSummaryResponse {

    private Long visitId;
    private Long summaryId;
    private LocalDateTime date;
    private Integer dentalDigitScore;
    private Integer missingTeeth;
    private Integer unsalvageableTeeth;
    private Integer affectedTeeth;
    private String notes;


}
