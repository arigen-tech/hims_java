package com.hims.mapper;

import com.hims.dto.MasRelationDto;
import com.hims.entity.MasRelation;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MasRelationMapper {
    MasRelation toEntity(MasRelationDto masRelationDto);

    MasRelationDto toDto(MasRelation masRelation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MasRelation partialUpdate(MasRelationDto masRelationDto, @MappingTarget MasRelation masRelation);
}
