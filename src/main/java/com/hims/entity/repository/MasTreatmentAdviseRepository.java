package com.hims.entity.repository;

import com.hims.entity.MasTreatmentAdvise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasTreatmentAdviseRepository extends JpaRepository<MasTreatmentAdvise, Long> {

    // Optional: find all treatment advice by department
    // List<TreatmentAdvise> findByDepartment_DepartmentId(Long departmentId);
}
