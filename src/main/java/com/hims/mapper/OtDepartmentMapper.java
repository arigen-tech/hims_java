package com.hims.mapper;

import com.hims.projection.OtDepartmentProjection;
import com.hims.response.OtDepartmentResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OtDepartmentMapper {
    OtDepartmentResponse toResponse(OtDepartmentProjection projection);

    List<OtDepartmentResponse> toResponseList(List<OtDepartmentProjection> projections);
}
