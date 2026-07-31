package com.hims.entity.repository;

import com.hims.entity.MasBloodTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBloodTestRepository
        extends JpaRepository<MasBloodTest, Long> {

    List<MasBloodTest> findByStatusIgnoreCaseOrderByTestNameAsc(String status);






    List<MasBloodTest> findAllByOrderByStatusDescCreatedDateDesc();
}

