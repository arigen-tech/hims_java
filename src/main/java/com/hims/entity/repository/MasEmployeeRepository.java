package com.hims.entity.repository;

import com.hims.entity.MasEmployee;
import com.hims.response.DoctorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasEmployeeRepository extends JpaRepository<MasEmployee, Long> {
    List<MasEmployee> findByStatus(String status);
    Optional<MasEmployee> findByMobileNo(String mobileNo);
    List<MasEmployee> findByRoleIdIdAndFirstNameContainingIgnoreCaseOrderByFirstNameAsc(Long roleId, String keyword);

}
