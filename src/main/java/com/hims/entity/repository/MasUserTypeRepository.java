package com.hims.entity.repository;

import com.hims.entity.MasUserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasUserTypeRepository extends JpaRepository<MasUserType, Long> {
//    List<MasUserType> findByStatusIgnoreCase(String y);
//
//    List<MasUserType> findByStatusInIgnoreCase(List<String> y);

    List<MasUserType> findByStatusIgnoreCaseOrderByUserTypeNameAsc(String y);

    List<MasUserType> findByStatusInIgnoreCaseOrderByLastChgDateDesc(List<String> y);

    List<MasUserType> findAllByOrderByStatusDescLastChgDateDesc();
}
