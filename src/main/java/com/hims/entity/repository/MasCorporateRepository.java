package com.hims.entity.repository;

import com.hims.entity.MasCorporate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasCorporateRepository extends JpaRepository<MasCorporate,Long> {
    List<MasCorporate> findByStatusIgnoreCaseOrderByCorporateNameAsc(String lowerCase);

    List<MasCorporate> findAllByOrderByStatusDescLastChgDateDesc();
}
