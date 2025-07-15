package com.hims.entity.repository;

import com.hims.entity.MasStoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasStoreItemRepository extends JpaRepository<MasStoreItem,Long> {
    List<MasStoreItem> findByStatusIgnoreCase(String y);

    List<MasStoreItem> findByStatusInIgnoreCase(List<String> y);

    Optional<MasStoreItem> findByPvmsNo(String code);



    Optional<MasStoreItem> findFirstByPvmsNoOrNomenclature(String pvmsNo, String nomenclature);

   
    Optional<MasStoreItem> findByPvmsNoAndItemIdNot(String pvmsNo, Long id);

    Optional<MasStoreItem> findByNomenclatureAndItemIdNot(String nomenclature, Long id);
}
