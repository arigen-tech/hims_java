package com.hims.entity.repository;

import com.hims.entity.MasCareLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasCareLevelRepo extends JpaRepository<MasCareLevel,Long> {

    List<MasCareLevel> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String status);

    List<MasCareLevel> findByStatusIgnoreCaseInOrderByLastUpdateDateDesc(List<String> statuses);

}
