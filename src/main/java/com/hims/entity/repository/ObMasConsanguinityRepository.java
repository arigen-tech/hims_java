package com.hims.entity.repository;

import com.hims.entity.ObMasConsanguinity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasConsanguinityRepository
        extends JpaRepository<ObMasConsanguinity, Long> {

    List<ObMasConsanguinity> findByStatusIgnoreCaseOrderByConsanguinityValueAsc(String status);

    List<ObMasConsanguinity> findAllByOrderByStatusDescLastUpdateDateDesc();
}
