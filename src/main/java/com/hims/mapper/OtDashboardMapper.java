package com.hims.mapper;

import com.hims.projection.OtDashboardProjection;
import com.hims.response.OtDashboardStatusCount;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OtDashboardMapper {
    @Mapping(target = "count", source = "bookingCount")
    OtDashboardStatusCount toStatusCount(OtDashboardProjection projection);
}
