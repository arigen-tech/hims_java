package com.hims.entity.repository;

import com.hims.entity.EmployeeMembership;
import com.hims.entity.MasEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EmployeeMembershipRepository extends JpaRepository<EmployeeMembership, Long> {

    List<EmployeeMembership> findByEmployee(MasEmployee employee);
}
