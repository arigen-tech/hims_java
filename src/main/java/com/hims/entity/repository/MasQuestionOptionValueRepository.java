package com.hims.entity.repository;

import com.hims.entity.MasQuestionOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasQuestionOptionValueRepository extends JpaRepository<MasQuestionOptionValue,Long> {
    List<MasQuestionOptionValue> findByStatusIgnoreCaseOrderByOptionValueAsc(String y);

    List<MasQuestionOptionValue> findAllByOrderByStatusDescLastUpdateDateDesc();
}
