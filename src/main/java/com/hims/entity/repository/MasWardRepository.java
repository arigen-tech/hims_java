package com.hims.entity.repository;

import com.hims.entity.MasWard;
import com.hims.response.IpdWardResponse;
import com.hims.response.WardResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasWardRepository extends JpaRepository<MasWard,Long> {
//    List<MasWard> findByStatusIgnoreCaseIn(List<String> y);
//
//    List<MasWard> findByStatusIgnoreCase(String y);

  //  List<MasWard> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(List<String> y);

    List<MasWard> findByStatusIgnoreCaseOrderByWardNameAsc(String y);

   // List<MasWard> findByStatusIgnoreCaseInOrderByLastUpdateDateDesc(List<String> y);

    List<MasWard> findAllByOrderByStatusDescLastUpdateDateDesc();

    List<MasWard> findByWardCategory_Id(Long wardCategoryId);

    List<MasWard> findByWardCategory_IdAndStatus(Long wardCategoryId, String status);

    @Query("""
        SELECT new com.hims.response.IpdWardResponse(
            w.wardId,
            w.wardName
        )
        FROM MasWard w
        WHERE w.department.id = :departmentId
          AND LOWER(w.status) =:status
        ORDER BY w.wardName ASC
    """)
    List<IpdWardResponse> getWardByDepartment(
            @Param("departmentId") Long departmentId,
            @Param("status") String status
    );

    @Query("""
        SELECT new com.hims.response.WardResponse(
            w.wardId,
            w.wardName,
            COUNT(DISTINCT b.bedId)
        )
        FROM MasWard w
        LEFT JOIN MasRoom r
            ON r.masWard.wardId = w.wardId
            AND LOWER(r.status) =:status
        LEFT JOIN MasBed b
            ON b.roomId.roomId = r.roomId
            AND LOWER(b.status) =:status
            AND b.bedStatusId.bedStatusId =:bedStatusId
        WHERE w.wardCategory.id = :wardCategoryId
          AND LOWER(w.status) =:status
        GROUP BY w.wardId, w.wardName
        ORDER BY w.wardName ASC
    """)
    List<WardResponse> getWardsByCategory(
            @Param("wardCategoryId") Long wardCategoryId,
            @Param("bedStatusId") Long bedStatusId,
            @Param("status") String status

    );
}
