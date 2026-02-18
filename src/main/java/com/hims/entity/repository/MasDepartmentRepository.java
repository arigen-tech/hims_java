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

    /**
     * Get department IDs by department type code
     * @param deptTypeCode Department type code from mas_department_type table
     * @return List of department IDs
     */
    @Query("""
        SELECT d.id
        FROM MasDepartment d
        WHERE  LOWER(d.departmentType.departmentTypeCode) = LOWER(:deptTypeCode)
          AND LOWER(d.status) = 'y'
    """)
    List<Long> findDepartmentIdsByDepartmentTypeCode(@Param("deptTypeCode") String deptTypeCode);

 /**
  * Get department IDs by department type ID
  * @param deptTypeId Department type Id from mas_department_type table
  * @return List of department IDs
  */
 @Query("""
        SELECT d.id
        FROM MasDepartment d
        WHERE d.departmentType.id = :deptTypeId
          AND LOWER(d.status) = 'y'
    """)
 List<Long> findDepartmentIdsByDepartmentTypeId(@Param("deptTypeId") Long deptTypeId);



    List<MasDepartment> findByIndentApplicableIgnoreCase(String indentApplicable);


    List<MasDepartment> findByHospitalIdAndDepartmentTypeIdAndDepartmentNameContainingIgnoreCaseOrderByDepartmentNameAsc(Long hospitalId, Long opdDeptTypeId, String searchInput);

    List<MasDepartment> findByHospitalIdAndDepartmentTypeId(Long hospitalId, Long opdId);

    List<MasDepartment> findByDepartmentTypeId(Long opdId);
    List<MasDepartment> findByDepartmentTypeId(int opdId);

 

    /**
     *  Fetches departments with all relationships in a single query.
     * Use this instead of findByHospitalIdAndDepartmentTypeIdAndDepartmentNameContainingIgnoreCase
     * to avoid N+1 problems when accessing departmentType, hospital, etc.
     */
    @Query("""
        SELECT DISTINCT d
        FROM MasDepartment d
        LEFT JOIN FETCH d.departmentType dt
        LEFT JOIN FETCH d.hospital h
        LEFT JOIN FETCH d.careLevel cl
        LEFT JOIN FETCH d.wardCategory wc
        WHERE d.hospital.id = :hospitalId
          AND d.departmentType.id = :deptTypeId
          AND LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :searchInput, '%'))
        ORDER BY d.departmentName ASC
        """)
    List<MasDepartment> findByHospitalAndDeptTypeAndNameWithSearchInput(
        @Param("hospitalId") Long hospitalId,
        @Param("deptTypeId") Long deptTypeId,
        @Param("searchInput") String searchInput
    );




    /**
     * Fetches departments by type with keyword search and all relationships.
     * Use instead of findByDepartmentTypeIdAndDepartmentNameContainingIgnoreCase to avoid N+1.
     */
    @Query("""
        SELECT DISTINCT d
        FROM MasDepartment d
        LEFT JOIN FETCH d.departmentType dt
        LEFT JOIN FETCH d.hospital h
        LEFT JOIN FETCH d.careLevel cl
        LEFT JOIN FETCH d.wardCategory wc
        WHERE d.departmentType.id = :deptTypeId
          AND LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :searchInput, '%'))
        ORDER BY d.departmentName ASC
        """)
    List<MasDepartment> findByTypeWithSearchInput(
        @Param("deptTypeId") Long deptTypeId,
        @Param("searchInput") String searchInput
    );




}

