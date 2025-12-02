package com.hims.entity.repository;

import com.hims.entity.MasBedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBedTypeRepository extends JpaRepository<MasBedType,Long> {
    List<MasBedType> findByStatus(String y);


    List<MasBedType> findByStatusIgnoreCaseInOrderByLastUpdateDateDesc(List<String> y);

    List<MasBedType> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);
}
