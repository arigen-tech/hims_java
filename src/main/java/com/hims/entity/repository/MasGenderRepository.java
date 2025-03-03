package com.hims.entity.repository;

import com.hims.entity.MasGender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasGenderRepository extends JpaRepository<MasGender, Long> {

    List<MasGender> findByStatusOrderByGenderNameAsc(String status);
    Optional<MasGender> findByGenderCode(String genderCode);
}
