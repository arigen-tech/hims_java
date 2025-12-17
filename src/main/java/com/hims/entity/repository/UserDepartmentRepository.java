package com.hims.entity.repository;

import com.hims.entity.MasDepartment;
import com.hims.entity.User;
import com.hims.entity.UserDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {
    List<UserDepartment> findByDepartment(MasDepartment masDepartment);
    List<UserDepartment> findByUser_UserId(long userId);

    List<UserDepartment> findByUser_UserIdAndStatus(Long userId, String status);

    List<UserDepartment> findByUser_UserNameAndUser_StatusAndStatus(String userName, String userStatus, String departmentStatus);


    @Query("""
    SELECT DISTINCT ud.user
    FROM UserDepartment ud
    WHERE ud.department.id = :departmentId
    AND LOWER(ud.status) = 'y'
""")
    List<User> findUsersByDepartment(@Param("departmentId") Long departmentId);



    List<UserDepartment> findAllByOrderByUserAsc();
}
