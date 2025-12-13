package com.hims.entity.repository;

import com.hims.entity.MasTreatmentAdvise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasTreatmentAdviseRepository extends JpaRepository<MasTreatmentAdvise, Long> {
    List<MasTreatmentAdvise> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

   // List<MasTreatmentAdvise> findByStatusIgnoreCaseInOrderByLastUpdateDateDesc(List<String> y);



    List<MasTreatmentAdvise> findByStatusIgnoreCaseOrderByTreatmentAdviceAsc(String y);

    List<MasTreatmentAdvise> findAllByOrderByLastUpdateDateDesc();

    // Optional: find all treatment advice by department
    // List<TreatmentAdvise> findByDepartment_DepartmentId(Long departmentId);
}
