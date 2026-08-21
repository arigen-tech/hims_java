
package com.hims.entity.repository;

import com.hims.entity.StoreReturnM;
import com.hims.response.UnverifiedReturnHeaderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface StoreReturnMRepository extends JpaRepository<StoreReturnM, Long> {

    StoreReturnM findByStoreIndentReceiveM_StoreInternalIndent_IndentMId(Long indentMId);

    @Query("""
       SELECT r.returnMId
       FROM StoreReturnM r
       WHERE r.storeIndentReceiveM.storeInternalIndent.indentMId = :indentMId
       """)
    Long findReturnMIdByIndentMId(@Param("indentMId") Long indentMId);

    @Query("""
    SELECT new com.hims.response.UnverifiedReturnHeaderResponse(
        r.returnMId,
        r.returnNo,
        rem.receiveMId,
        recd.id,
        recd.departmentName,
        isd.id,
        isd.departmentName,
        r.returnDate,
        r.returnedBy
    )
    FROM StoreReturnM r
    LEFT JOIN r.storeDepartment isd
    LEFT JOIN isd.hospital h
    LEFT JOIN  r.storeIndentReceiveM rem
    LEFT JOIN rem.receivedDepartment recd
    
    WHERE isd.id = :toDepartmentId

      AND (
            :fromDepartmentId IS NULL
            OR recd.id = :fromDepartmentId
          )

       AND r.returnDate >= COALESCE(:returnFromDate, r.returnDate)
           
       AND r.returnDate <= COALESCE(:returnToDate, r.returnDate)

      AND (
             r.status = 'N'
          )
      AND h.id=:hospitalId
    ORDER BY r.returnDate DESC
    """)
    Page<UnverifiedReturnHeaderResponse> getUnverifiedReturnHeaders(Long hospitalId,
                                                                    Long toDepartmentId,
                                                                    LocalDateTime returnFromDate,
                                                                    LocalDateTime returnToDate,
                                                                    Long fromDepartmentId,
                                                                    Pageable pageable);
}
