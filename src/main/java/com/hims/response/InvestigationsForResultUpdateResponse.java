package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InvestigationsForResultUpdateResponse {



        private Long resultEntryHeaderId;
        private Long resultEntryDetailsId;
        private Long investigationId;
        private String investigationName;
        private String unit;
        private String sampleName;
        private String remarks;
        private String result;
        private String normalValue;
        private String generatedSampleId;
        private String investigationType;

}
