package com.hims.entity.repository;

import com.hims.entity.MasDepartment;
import com.hims.response.SpecialitiesResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasDepartmentRepository extends JpaRepository<MasDepartment, Long> {
   // MasDepartment findById(Long amountTypeId);
   List<MasDepartment> findByStatusIgnoreCase(String status);
   List<MasDepartment> findByStatusInIgnoreCase(List<String> statuses);

    List<MasDepartment> findByIdIn(List<Long> fixedIds);

    List<MasDepartment> findByStatusIgnoreCaseOrderByDepartmentNameAsc(String y);

    @Query("""
        SELECT m
        FROM MasDepartment m
        WHERE m.departmentType.id = :departmentTypeId
          AND m.wardCategory.id = :wardCategoryId
          AND LOWER(m.status) = 'y'
    """)
    List<MasDepartment> findActiveWardDepartments(
            @Param("departmentTypeId") Long departmentTypeId,
            @Param("wardCategoryId") Long wardCategoryId
    );

    List<MasDepartment> findAllByOrderByStatusDescLastChgDateDescLastChgTimeDesc();

    List<MasDepartment> findByDepartmentTypeIdAndDepartmentNameContainingIgnoreCaseOrderByDepartmentNameAsc(Long opdId, String keyword);

    

    List<MasDepartment> findByIndentApplicableIgnoreCase(String indentApplicable);


    List<MasDepartment> findByHospitalIdAndDepartmentTypeIdAndDepartmentNameContainingIgnoreCaseOrderByDepartmentNameAsc(Long hospitalId, Long opdId, String keyword);

    List<MasDepartment> findByHospitalIdAndDepartmentTypeId(Long hospitalId, Long opdId);

    List<MasDepartment> findByDepartmentTypeId(Long opdId);
    List<MasDepartment> findByDepartmentTypeId(int opdId);
}

