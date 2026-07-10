package com.hims.response;

import lombok.Data;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
public class AvailableTokenSlotResponse {
    private boolean isAvailable;
    private int tokenNo;
    private LocalTime startTime;
    private LocalTime endTime;

    public AvailableTokenSlotResponse(int tokenNo, LocalTime startTime, LocalTime endTime, boolean isAvailable) {
        this.tokenNo = tokenNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
    }


    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return String.format("Token %d: %s - %s",
                tokenNo,
                startTime.format(fmt),
                endTime.format(fmt));
    }

}
