package com.hims.entity.repository;

import com.hims.entity.MasCommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasCommonStatusRepository extends JpaRepository<MasCommonStatus,Long> {
    List<MasCommonStatus> findAllByOrderByUpdateDateDesc();

    Optional<MasCommonStatus> findByEntityNameAndColumnNameAndStatusCode(String entityName, String columnName, String statusCode);
    List<MasCommonStatus> findByEntityNameInAndColumnNameIn(List<String> entities,List<String> columns );
}
