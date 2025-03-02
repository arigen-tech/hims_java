package com.hims.mapper;

import com.hims.dto.MasGenderDto;
import com.hims.entity.MasGender;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MasGenderMapper {
    MasGender toEntity(MasGenderDto masGenderDto);

    MasGenderDto toDto(MasGender masGender);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MasGender partialUpdate(MasGenderDto masGenderDto, @MappingTarget MasGender masGender);
}
