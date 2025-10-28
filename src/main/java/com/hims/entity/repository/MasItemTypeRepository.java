package com.hims.entity.repository;

import com.hims.entity.MasItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasItemTypeRepository extends JpaRepository<MasItemType,Integer> {
    List<MasItemType> findByStatusIgnoreCase(String y);

    List<MasItemType> findByStatusInIgnoreCase(List<String> y);




    List<MasItemType> findByMasStoreGroupIdId(Long id);
}
