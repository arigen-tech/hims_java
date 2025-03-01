package com.hims.mapper;

import com.hims.dto.PatientDto;
import com.hims.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);

    Patient toEntity(PatientDto dto);

    PatientDto toDto(Patient entity);
}
