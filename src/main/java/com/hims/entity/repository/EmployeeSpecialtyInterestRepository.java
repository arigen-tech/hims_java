package com.hims.entity.repository;

import com.hims.entity.EmployeeSpecialtyInterest;
import com.hims.entity.MasEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.Collection;
import java.util.List;

@Repository
public interface EmployeeSpecialtyInterestRepository extends JpaRepository<EmployeeSpecialtyInterest, Long> {
    List<EmployeeSpecialtyInterest> findByEmployee(MasEmployee employee);
}