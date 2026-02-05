package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInternalIndentMRepository extends JpaRepository<StoreInternalIndentM,Long> , JpaSpecificationExecutor<StoreInternalIndentM> {

    Optional<StoreInternalIndentM> findTopByOrderByIndentMIdDesc();

    // fromDeptId is MasDepartment, whose PK field name is "id"
    List<StoreInternalIndentM> findByFromDeptId_Id(Long deptId);

    // ✅ fixed: use Id (field name), not DepartmentId
    List<StoreInternalIndentM> findByFromDeptId_IdAndStatus(Long deptId, String status);

    List<StoreInternalIndentM> findByStatus(String status);


    List<StoreInternalIndentM> findByStatusOrderByIndentMIdDesc(String status);
    List<StoreInternalIndentM> findAllByOrderByIndentMIdDesc();


    List<StoreInternalIndentM> findByFromDeptId_IdAndStatusIn(Long deptId, List<String> allowedStatuses);

    // ✅ NEW: use toDeptId instead of fromDeptId
    List<StoreInternalIndentM> findByToDeptId_IdAndStatus(Long deptId, String status);

    // (optional) if you want sorted directly from DB:
    List<StoreInternalIndentM> findByToDeptId_IdAndStatusOrderByIndentMIdDesc(Long deptId, String status);


    List<StoreInternalIndentM> findByToDeptId_IdAndStatusIn(Long deptId, List<String> statuses);

    List<StoreInternalIndentM> findByFromDeptId_IdAndStatusAndIssuedDateBetween(
            Long fromDeptId,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    Page<StoreInternalIndentM> findByFromDeptId_Id(Long deptId, Pageable pageable);

    Page<StoreInternalIndentM> findByFromDeptId_IdIn(List<Long> deptIds, Pageable pageable);


}
