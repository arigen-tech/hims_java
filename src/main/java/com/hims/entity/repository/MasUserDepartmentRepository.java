package com.hims.entity.repository;

import com.hims.entity.MasUserDepartment;
import com.hims.response.MasUserDepartmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasUserDepartmentRepository extends JpaRepository<MasUserDepartment, Long> {

    @Query("SELECT new com.hims.response.MasUserDepartmentResponse(mud.userDepartmentId, u.userId, u.userName, d.id, d.departmentName) " +
            "FROM MasUserDepartment mud " +
            "JOIN mud.user u " +
            "JOIN mud.department d")
    List<MasUserDepartmentResponse> fetchAllUserDepartments();


    @Query("SELECT new com.hims.response.MasUserDepartmentResponse( " +
            "mud.userDepartmentId, u.userId, u.userName, d.id, d.departmentName) " +
            "FROM MasUserDepartment mud " +
            "JOIN mud.user u " +
            "JOIN mud.department d " +
            "WHERE d.id = :departmentId")
    List<MasUserDepartmentResponse> fetchByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT new com.hims.response.MasUserDepartmentResponse( " +
            "mud.userDepartmentId, u.userId, u.userName, d.id, d.departmentName) " +
            "FROM MasUserDepartment mud " +
            "JOIN mud.user u " +
            "JOIN mud.department d " +
            "WHERE u.userId = :userId AND mud.status = 'y'")
    List<MasUserDepartmentResponse> fetchByUserId(@Param("userId") Long userId);



}
