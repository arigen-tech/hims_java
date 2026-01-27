package com.hims.entity.repository;

import com.hims.entity.MasBloodComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MasBloodComponentRepository extends JpaRepository<MasBloodComponent,Long> {
    List<MasBloodComponent> findByStatusIgnoreCaseOrderByComponentNameAsc(String y);


    List<MasBloodComponent> findAllByOrderByStatusDescLastUpdateDateDesc();
}
