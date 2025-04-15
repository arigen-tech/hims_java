package com.hims.response;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasFrequencyResponse {
        private Long frequencyId;
//        private  String frequencyCode;
        private String frequencyName;
        private String status;
        private String lastChgBy;
        private Instant lastChgDate;
        private String lastChgTime;
        private Double feq;
//        private String frequency;
        private Long orderNo;
}
