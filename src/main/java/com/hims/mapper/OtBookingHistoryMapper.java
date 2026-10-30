package com.hims.mapper;

import com.hims.projection.OtBookingHistoryProjection;
import com.hims.projection.PendingForOtSurgeryProjection;
import com.hims.response.OtBookingHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OtBookingHistoryMapper {
    @Mapping(target = "otBookingId", source = "id")
    @Mapping(target = "otBookingRequestId", source = "headerid")
    @Mapping(target = "surgeries", ignore = true)
    OtBookingHistoryResponse toResponse(OtBookingHistoryProjection projection);

    @Mapping(target = "otBookingRequestDtId", source = "otBookingDtId")
    OtBookingHistoryResponse.SurgeryDetail toSurgeryResponse(PendingForOtSurgeryProjection projection);
}
