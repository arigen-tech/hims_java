package com.hims.response;

import java.time.LocalDate;
import java.util.List;

public record OtDashboardResponse(
        LocalDate scheduledDate,
        List<OtDashboardStatusCount> statuses,
        String otNames) {
}
