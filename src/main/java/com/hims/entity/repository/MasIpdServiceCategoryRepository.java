package com.hims.entity.repository;

import com.hims.entity.MasIpdServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasIpdServiceCategoryRepository extends JpaRepository<MasIpdServiceCategory,Long> {
    List<MasIpdServiceCategory> findByStatusIgnoreCaseOrderByCategoryNameAsc(String lowerCase);

    List<MasIpdServiceCategory> findAllByOrderByStatusDescLastChgDateDesc();
}
