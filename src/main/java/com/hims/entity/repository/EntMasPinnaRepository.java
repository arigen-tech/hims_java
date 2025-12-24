package com.hims.entity.repository;

import com.hims.entity.EntMasPinna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntMasPinnaRepository
        extends JpaRepository<EntMasPinna, Long> {

    List<EntMasPinna>
    findByStatusIgnoreCaseOrderByPinnaStatusAsc(String status);

    List<EntMasPinna>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
