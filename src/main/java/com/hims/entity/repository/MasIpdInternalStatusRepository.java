package com.hims.entity.repository;

import com.hims.entity.MasIpdInternalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasIpdInternalStatusRepository extends JpaRepository<MasIpdInternalStatus,Long> {
}
