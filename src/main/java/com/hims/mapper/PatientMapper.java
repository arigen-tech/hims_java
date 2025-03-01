package com.hims.mapper;

import com.hims.dto.PatientDto;
import com.hims.entity.Patient;

@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "spring")
public interface PatientMapper {
    Patient toEntity(PatientDto patientDto);

    PatientDto toDto(Patient patient);

    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    Patient partialUpdate(PatientDto patientDto, @org.mapstruct.MappingTarget Patient patient);
}
