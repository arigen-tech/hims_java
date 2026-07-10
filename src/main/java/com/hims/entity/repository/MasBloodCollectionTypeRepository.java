package com.hims.entity.repository;

import com.hims.entity.MasBloodCollectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBloodCollectionTypeRepository
        extends JpaRepository<MasBloodCollectionType, Long> {

    List<MasBloodCollectionType>
    findByStatusIgnoreCaseOrderByCollectionTypeNameAsc(String status);

    List<MasBloodCollectionType>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
