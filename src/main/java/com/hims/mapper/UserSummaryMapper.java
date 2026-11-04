package com.hims.mapper;

import com.hims.response.UserSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class UserSummaryMapper {

    public UserSummaryResponse toResponse(Object[] row) {
        return new UserSummaryResponse(
                toLong(row[0]),
                toStringValue(row[1]),
                toStringValue(row[2]),
                toLong(row[3]),
                toStringValue(row[4]));
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
