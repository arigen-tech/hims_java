package com.hims.entity.repository;

import com.hims.entity.MasBloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasBloodGroupRepository extends JpaRepository<MasBloodGroup, Long> {

    List<MasBloodGroup> findByStatusIgnoreCase(String status);

    List<MasBloodGroup> findByStatusInIgnoreCase(List<String> statuses);

    List<MasBloodGroup> findByStatusIgnoreCaseOrderByBloodGroupNameAsc(String y);

   // List<MasBloodGroup> findByStatusIgnoreCaseInOrderByLastChangedDateDesc(List<String> y);



   // List<MasBloodGroup> findByStatusIgnoreCaseInOrderByLastChangedDateDescLastChangedTimeDesc(List<String> y);

    List<MasBloodGroup> findAllByOrderByStatusDescLastChangedDateDescLastChangedTimeDesc();
}
