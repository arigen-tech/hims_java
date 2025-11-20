package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInternalIndentMRepository extends JpaRepository<StoreInternalIndentM,Long> {

    Optional<StoreInternalIndentM> findTopByOrderByIndentMIdDesc();

    // fromDeptId is MasDepartment, whose PK field name is "id"
    List<StoreInternalIndentM> findByFromDeptId_Id(Long deptId);

    // ✅ fixed: use Id (field name), not DepartmentId
    List<StoreInternalIndentM> findByFromDeptId_IdAndStatus(Long deptId, String status);

    List<StoreInternalIndentM> findByStatus(String status);
}
