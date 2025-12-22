package com.hims.entity.repository;

import com.hims.entity.ObMasPresentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasPresentationRepository
        extends JpaRepository<ObMasPresentation, Long> {

    List<ObMasPresentation>
    findByStatusIgnoreCaseOrderByPresentationValueAsc(String status);

    List<ObMasPresentation>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
