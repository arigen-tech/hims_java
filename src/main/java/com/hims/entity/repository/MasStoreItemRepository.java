package com.hims.entity.repository;

import com.hims.entity.MasStoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasStoreItemRepository extends JpaRepository<MasStoreItem,Long> {
    List<MasStoreItem> findByStatusIgnoreCase(String y);

    List<MasStoreItem> findByStatusInIgnoreCase(List<String> y);
}
