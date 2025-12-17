package com.hims.entity.repository;

import com.hims.entity.MasGender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasGenderRepository extends JpaRepository<MasGender, Long> {

//    List<MasGender> findByStatusOrderByGenderNameAsc(String status);
//    Optional<MasGender> findByGenderCode(String genderCode);
    List<MasGender> findByStatusIgnoreCase(String status);
    List<MasGender> findByStatusInIgnoreCase(List<String> statuses);

    List<MasGender> findByStatusIgnoreCaseOrderByGenderNameAsc(String y);

    List<MasGender> findByStatusIgnoreCaseInOrderByLastChgDtDesc(List<String> y);

    List<MasGender> findAllByOrderByStatusDescLastChgDtDesc();
}
