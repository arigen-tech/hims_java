package com.hims.entity.repository;

import com.hims.entity.MasAnaesthesiaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasAnaesthesiaTypeRepository extends JpaRepository<MasAnaesthesiaType, Long> {

    List<MasAnaesthesiaType> findByStatusIgnoreCaseOrderByAnaesthesiaTypeNameAsc(String status);

    List<MasAnaesthesiaType> findAllByOrderByStatusDescLastChgDateDesc();
}
