package com.hims.entity.repository;

import com.hims.entity.MasBed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBedRepository extends JpaRepository<MasBed,Long> {
    List<MasBed> findByStatusIgnoreCaseIn(List<String> y);

    List<MasBed> findByStatusIgnoreCase(String y);

    List<MasBed> findAllByOrderByStatusDescLastUpdateDateDesc();
}
