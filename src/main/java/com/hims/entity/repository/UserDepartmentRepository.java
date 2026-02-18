  //  List<UserDepartment> findByDepartmentId(List<MasDepartment> departments2);

   // Optional<UserDepartment> findByUser_Employee_EmployeeId(Long doctorId);





package com.hims.entity.repository;

import com.hims.entity.MasDepartment;
import com.hims.entity.User;
import com.hims.entity.UserDepartment;
import com.hims.response.SpecialitiesResponse;
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

}
