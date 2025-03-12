package com.hims.entity.repository;

import com.hims.entity.MasRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasRelationRepository extends JpaRepository<MasRelation, Long> {

    List<MasRelation> findByStatusIgnoreCase(String status);

    List<MasRelation> findByStatusInIgnoreCase(List<String> statuses);

}
