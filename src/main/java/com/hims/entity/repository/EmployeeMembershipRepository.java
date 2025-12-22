package com.hims.entity.repository;

import com.hims.entity.EmployeeMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeMembershipRepository extends JpaRepository<EmployeeMembership, Long> {
}
