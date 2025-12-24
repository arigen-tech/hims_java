package com.hims.entity.repository;

import com.hims.entity.MasQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasQuestionRepository
        extends JpaRepository<MasQuestion, Long> {


    List<MasQuestion>
    findAllByOrderByStatusDescLastUpdateDateDesc();

    List<MasQuestion> findByStatusIgnoreCaseOrderByQuestionAsc(String y);
}
