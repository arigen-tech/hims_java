package com.hims.entity.repository;

import com.hims.entity.MasRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasRouteRepository extends JpaRepository<MasRoute, Long> {


   // List<MasRoute> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

    List<MasRoute> findAllByOrderByLastUpdateDateDesc();

    List<MasRoute> findByStatusIgnoreCaseOrderByRouteNameAsc(String y);
}
