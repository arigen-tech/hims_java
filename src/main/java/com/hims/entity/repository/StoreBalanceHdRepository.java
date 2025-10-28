package com.hims.entity.repository;

import com.hims.entity.StoreBalanceHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import java.util.List;
import java.util.Optional;

@Repository
public interface StoreBalanceHdRepository extends JpaRepository<StoreBalanceHd,Long> {
    List<StoreBalanceHd> findByStatus(String status);

    List<StoreBalanceHd> findByStatusIn(List<String> statusList);
    Optional<StoreBalanceHd> findByBalanceMIdAndHospitalIdIdAndDepartmentIdId(Long id, Long hospitalId, Long departmentId);

    List<StoreBalanceHd> findByStatusInAndHospitalIdIdAndDepartmentIdId(List<String> list, Long hospitalId, Long departmentId);
}
