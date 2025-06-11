package com.hims.entity.repository;

import com.hims.entity.MasStoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasStoreItemRepository extends JpaRepository<MasStoreItem,Integer> {
}
