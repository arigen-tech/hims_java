package com.hims.entity.repository;

import com.hims.entity.MasHSN;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasHsnRepository extends JpaRepository<MasHSN,String> {
    List<MasHSN> findByStatusIgnoreCase(String y);

    List<MasHSN> findByStatusInIgnoreCase(List<String> y);

    Optional<MasHSN> findByHsnCode(MasHSN hsnCode);

    List<MasHSN> findAllByOrderByStatusDescLastUpdatedDtDesc();
}
