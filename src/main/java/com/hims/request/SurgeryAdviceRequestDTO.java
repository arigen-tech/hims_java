package com.hims.request;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class SurgeryAdviceRequestDTO {

    private Long otId;
    private Long otHdId;
    private LocalDate surgeryDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime surgeryStartTime;
    private LocalTime surgeryEndTime;

    private List<SurgeryDetailDTO> surgeryDetails;

    @Data
    @Builder
    public static class SurgeryDetailDTO {
        private Long otDtId;
        private Long surgeryId;
        private String surgeryName;
    }
}