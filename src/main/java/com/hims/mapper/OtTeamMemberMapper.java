package com.hims.mapper;

import com.hims.entity.OtTeamMember;
import com.hims.projection.OtTeamMemberProjection;
import com.hims.response.OtTeamMemberDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface OtTeamMemberMapper {

    @Mapping(target = "otTeamRoleId", source = "otTeamRole.otTeamRoleId")
    @Mapping(target = "roleName", source = "otTeamRole.roleName")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userFullName", expression = "java(fullName(member))")
    OtTeamMemberDetailResponse toResponse(OtTeamMember member);

    OtTeamMemberDetailResponse toResponse(OtTeamMemberProjection projection);

    default String fullName(OtTeamMember member) {
        return Stream.of(member.getUser().getFirstName(), member.getUser().getMiddleName(), member.getUser().getLastName())
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining(" "));
    }
}
