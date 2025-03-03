package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link com.hims.entity.AppSetup}
 */
public record AppSetupDto(Integer id, @Size(max = 8) String fromTime, @Size(max = 8) String toTime, Integer maxNoOfDays,
                          Integer minNoOfDays, @Size(max = 45) String days, MasHospitalDto hospital,
                          MasDepartmentDto dept, Instant validFrom, Instant validTo, Integer dayOfWeek,
                          Integer doctorId, Integer totalToken, Integer totalInterval, Integer startToken,
                          MasOpdSessionDto session, Integer totalOnlineToken, Integer timeTaken,
                          @Size(max = 5) String startTime, @Size(max = 5) String endTime) implements Serializable {
}
