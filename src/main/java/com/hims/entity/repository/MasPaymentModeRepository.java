package com.hims.entity.repository;

import com.hims.entity.MasPaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasPaymentModeRepository extends JpaRepository<MasPaymentMode,Long> {

    List<MasPaymentMode> findByStatusIgnoreCaseOrderByModeNameAsc(String lowerCase);

    List<MasPaymentMode> findAllByOrderByStatusDescLastChgDateDesc();

    Optional<MasPaymentMode> findByModeCodeIgnoreCase(String value);
}
