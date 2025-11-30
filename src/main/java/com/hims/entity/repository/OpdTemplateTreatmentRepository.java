package com.hims.entity.repository;



import com.hims.entity.OpdTemplate;
import com.hims.entity.OpdTemplateTreatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpdTemplateTreatmentRepository extends JpaRepository<OpdTemplateTreatment, Long> {

    List<OpdTemplateTreatment> findByTemplate(OpdTemplate template);
    void deleteAllByTemplate(OpdTemplate template);
}

