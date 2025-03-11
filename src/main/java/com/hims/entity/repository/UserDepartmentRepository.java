package com.hims.entity.repository;

import com.hims.entity.MasDepartment;
import com.hims.entity.UserDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {
    List<UserDepartment> findByDepartment(MasDepartment masDepartment);
}
