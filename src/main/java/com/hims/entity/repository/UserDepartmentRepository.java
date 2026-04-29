  //  List<UserDepartment> findByDepartmentId(List<MasDepartment> departments2);

   // Optional<UserDepartment> findByUser_Employee_EmployeeId(Long doctorId);





package com.hims.entity.repository;

import com.hims.entity.MasDepartment;
import com.hims.entity.User;
import com.hims.entity.UserDepartment;
import com.hims.projection.UserDepartmentProjection;
import com.hims.response.SpecialitiesResponse;
import com.hims.response.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {
    List<UserDepartment> findByDepartment(MasDepartment masDepartment);
    List<UserDepartment> findByUser_UserId(long userId);

    List<UserDepartment> findByUser_UserIdAndStatus(Long userId, String status);

    List<UserDepartment> findByUser_UserNameAndUser_StatusAndStatusOrderByDepartment_DepartmentNameAsc(String userName, String userStatus, String departmentStatus);


    @Query("""
    SELECT DISTINCT ud.user
    FROM UserDepartment ud
    WHERE ud.department.id = :departmentId
    AND LOWER(ud.status) = 'y'
""")
    List<User> findUsersByDepartment(@Param("departmentId") Long departmentId);



    List<UserDepartment> findAllByOrderByUserAsc();

    @Query("""
        SELECT ud
        FROM UserDepartment ud
        JOIN FETCH ud.user u
        JOIN FETCH u.employee e
        WHERE ud.department.id = :departmentId

    """)
    List<UserDepartment> findByDepartmentIds(
            @Param("departmentId") Long departmentId
    );


    List<UserDepartment> findByUserUserId(Long userId);


    List<UserDepartment> findByDepartment_IdIn(List<Long> departmentIds);

    @Query(value = """
    SELECT 
        ud.user_department_id AS id,
        u.user_id AS userId,
        u.user_name AS username,
        d.department_id AS departmentId,
        d.department_name AS departmentName,
        ud.last_chg_by AS lastChgBy
       
    FROM user_department ud
    JOIN users u ON u.user_id = ud.user_id
    JOIN mas_department d ON d.department_id = ud.department_id
    WHERE u.user_name = :userName
      AND u.status = :status
      AND ud.status = :status
    ORDER BY d.department_name ASC
""", nativeQuery = true)
    List<UserDepartmentProjection> findAllUserDepartments(@Param("userName") String userName,
                                                          @Param("status") String status
    );

    @Query("""
        SELECT new com.hims.response.UserResponse(
            u.userId,
            u.firstName,
            u.middleName,
            u.lastName,
            u.roleId,
            u.userName,
            u.email,
            u.mobileNo,
            u.status,
            u.userType
        )
        FROM UserDepartment ud
        JOIN ud.user u
        WHERE ud.department.id = :deptId
    """)
    List<UserResponse> getUsersByDepartment(@Param("deptId") Long deptId);
}
