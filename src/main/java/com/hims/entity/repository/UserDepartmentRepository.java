package com.hims.entity.repository;

import com.hims.entity.MasDepartment;
import com.hims.entity.UserDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {
    List<UserDepartment> findByDepartment(MasDepartment masDepartment);
    List<UserDepartment> findByUser_UserId(long userId);

    List<UserDepartment> findByUser_UserIdAndStatus(Long userId, String status);

    List<UserDepartment> findByUser_UserNameAndUser_StatusAndStatus(String userName, String userStatus, String departmentStatus);





}
