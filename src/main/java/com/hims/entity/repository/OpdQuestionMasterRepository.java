package com.hims.entity.repository;

import com.hims.entity.OpdQuestionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpdQuestionMasterRepository extends JpaRepository<OpdQuestionMaster,Long> {
    List<OpdQuestionMaster> findByStatusIgnoreCaseOrderByQuestionAsc(String y);

    List<OpdQuestionMaster> findAllByOrderByStatusDescLastUpdateDateDesc();



    List<OpdQuestionMaster> findByQuestionHeading_QuestionHeadingIdAndStatus(Long questionHeadingId, String lowerCase);
}
