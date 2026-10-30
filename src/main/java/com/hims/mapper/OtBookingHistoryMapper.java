package com.hims.mapper;

import com.hims.projection.OtBookingHistoryProjection;
import com.hims.response.OtBookingHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OtBookingHistoryMapper {
    @Mapping(target = "otBookingId", source = "id")
    @Mapping(target = "otBookingRequestId", source = "headerid")
    @Mapping(target = "otBookingRequestDtId", source = "detailid")
    OtBookingHistoryResponse toResponse(OtBookingHistoryProjection projection);
}
