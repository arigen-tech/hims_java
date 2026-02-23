package com.hims.entity.repository;

import com.hims.entity.MasPacsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RadiologyTemplateRepository extends JpaRepository<MasPacsTemplate,Long> {
    List<MasPacsTemplate> findByStatusIgnoreCaseOrderByTemplateNameAsc(String y);

    List<MasPacsTemplate> findAllByOrderByPacsTemplateIdAsc();

    List<MasPacsTemplate> findAllByOrderByLastUpdateDateDesc();
}
