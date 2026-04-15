package com.hims.entity.repository;

import com.hims.entity.MasAdmissionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasAdmissionCategoryRepository extends JpaRepository<MasAdmissionCategory,Long> {
    List<MasAdmissionCategory> findByStatusIgnoreCaseOrderByAdmissionCategoryNameAsc(String lowerCase);

    List<MasAdmissionCategory> findAllByOrderByStatusDescLastUpdateDateDesc();
}
