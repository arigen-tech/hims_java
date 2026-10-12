package com.hims.entity.repository;

import com.hims.entity.MasResultFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MasResultFlagRepository extends JpaRepository<MasResultFlag, Long> {
    MasResultFlag findByFlagCode(String flagCode);
    List<MasResultFlag> findByStatus(String status);
}