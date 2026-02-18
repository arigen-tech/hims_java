package com.hims.entity.repository;


import com.hims.entity.MasEmployee;
import com.hims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    User findByUserNameAndStatus(String username , String isActive);

    User findByUserName(String email);

    Optional<User> findByEmployee(MasEmployee employee);

    User findByPhoneNumber(String phoneNo);

    User findByMobileNo(String mobileNo);


    User findByPhoneNumberAndStatus(String userName , String isActive);

    List<User> findByStatusIgnoreCase(String y);

    List<User> findByStatusInIgnoreCase(List<String> y);

    List<User> findByStatusIgnoreCaseOrderByUserNameAsc(String y);

    List<User> findAllByOrderByStatusDescLastChangeDateDesc();

    Optional<User> findByEmployeeEmployeeId(Long employeeId);

    User findByEmployee_EmployeeId(Long employeeId);

    /**
     * Batch fetch users by employee IDs to avoid N+1 query problem
     * @param employeeIds List of employee IDs
     * @return List of users matching the employee IDs
     */
    @Query("SELECT u FROM User u WHERE u.employee.employeeId IN :employeeIds")
    List<User> findUsersByEmployee_EmployeeIdIn(@Param("employeeIds") List<Long> employeeIds);

}
