package com.hims.entity.repository;

import com.hims.entity.MasQuestionHeading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasQuestionHeadingRepository
        extends JpaRepository<MasQuestionHeading, Long> {

    List<MasQuestionHeading>
    findByStatusIgnoreCaseOrderByQuestionHeadingNameAsc(String status);

    List<MasQuestionHeading>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
