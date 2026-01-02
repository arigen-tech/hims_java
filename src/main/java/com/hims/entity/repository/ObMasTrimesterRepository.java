package com.hims.entity.repository;

import com.hims.entity.ObMasTrimester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasTrimesterRepository
        extends JpaRepository<ObMasTrimester, Long> {

    List<ObMasTrimester>
    findByStatusIgnoreCaseOrderByTrimesterValueAsc(String status);

    List<ObMasTrimester>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
