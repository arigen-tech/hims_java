package com.hims.mapper;

import com.hims.entity.MasOtType;
import com.hims.response.MasOtTypeResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MasOtTypeMapper {

    MasOtTypeResponse toResponse(MasOtType entity);

    List<MasOtTypeResponse> toResponseList(List<MasOtType> entities);
}
