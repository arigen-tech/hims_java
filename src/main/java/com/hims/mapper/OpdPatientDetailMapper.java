package com.hims.mapper;

import com.hims.dto.OpdPatientDetailDto;
import com.hims.entity.OpdPatientDetail;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OpdPatientDetailMapper {
    OpdPatientDetail toEntity(OpdPatientDetailDto opdPatientDetailDto);

    OpdPatientDetailDto toDto(OpdPatientDetail opdPatientDetail);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    OpdPatientDetail partialUpdate(OpdPatientDetailDto opdPatientDetailDto, @MappingTarget OpdPatientDetail opdPatientDetail);
}
