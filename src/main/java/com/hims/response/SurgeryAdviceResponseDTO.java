package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SurgeryAdviceResponseDTO {

    private Long otBookingRequestHdId;
    private String otId;
    private LocalDate surgeryDate;
    private LocalTime surgeryStartTime;
    private LocalTime surgeryEndTime;
    private List<SurgeryDetailResponseDTO> surgeryDetails;


    @Data
    public static class SurgeryDetailResponseDTO {
        private Long otBookingRequestDtId;
        private Long surgeryId;
        private String surgeryName;
    }
}