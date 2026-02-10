package com.hims.response;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class AppSetupDTO {
    private String fromTime;
    private String toTime;
    private Long hospitalId;
    private Long deptId;
    private Instant validFrom;
    private Instant validTo;
    private Integer dayOfWeek;
    private Long doctorId;
    private Long sessionId;
    private String startTime;
    private String endTime;
    private Integer timeTaken;
    private List<appSetupDTO> days;

    @Data
    public static class appSetupDTO {
        private Long id;
        private String days;
        private String startTime;
        private String endTime;
        private Integer maxNoOfDays;
        private Integer minNoOfDays;
        private Integer totalToken;
        private Integer totalInterval;
        private Integer startToken;
        private Integer totalOnlineToken;
        private String opdLocation;

    }
}
