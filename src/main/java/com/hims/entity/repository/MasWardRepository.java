package com.hims.entity.repository;

import com.hims.entity.MasWard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasWardRepository extends JpaRepository<MasWard,Long> {
    List<MasWard> findByStatusIgnoreCaseIn(List<String> y);

    List<MasWard> findByStatusIgnoreCase(String y);
}
