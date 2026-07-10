package com.hims.entity.repository;

import com.hims.entity.MasBloodBagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBloodBagTypeRepository
        extends JpaRepository<MasBloodBagType, Long> {

    List<MasBloodBagType>
    findByStatusIgnoreCaseOrderByBagTypeNameAsc(String status);

    List<MasBloodBagType>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
