package com.hims.entity.repository;

import com.hims.entity.MasApplication;
import com.hims.projection.MasApplicationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasApplicationRepository extends JpaRepository<MasApplication, String> {
    List<MasApplication> findByStatusIgnoreCase(String status);
    List<MasApplication> findByStatusInIgnoreCase(List<String> statuses);
    @Query(value = "SELECT nextval('mas_application_order_seq')", nativeQuery = true)
    Long getNextOrderNo();

    @Query(value = "SELECT COALESCE(MAX(app_sequence_no), 0) + 1 FROM mas_application WHERE parent_id = :parentId", nativeQuery = true)
    Long getNextAppSequenceNo(@Param("parentId") String parentId);
    List<MasApplication> findByParentId(String parentId);
  //  List<MasApplication> findByParentIdIsNullOrParentId(String parentId);

  //  List<MasApplication> findByParentIdAndStatusIgnoreCase(String parentId, String status);


    List<MasApplication> findByStatusIgnoreCaseOrderByNameAsc(String y);

   // List<MasApplication> findByStatusIgnoreCaseInOrderByLastChgDateDesc(List<String> y);

    List<MasApplication> findAllByOrderByLastChgDateDesc();





    List<MasApplication> findAllByOrderByStatusDescLastChgDateDesc();

    @Query(value = """
            WITH RECURSIVE app_tree AS (
                SELECT
                    ma.app_id,
                    ma.name,
                    ma.parent_id,
                    ma.url,
                    ma.order_no,
                    ma.status,
                    ma.last_chg_date,
                    ma.app_sequence_no
                FROM mas_application ma
                WHERE ma.app_id = :parentId

                UNION ALL

                SELECT
                    child.app_id,
                    child.name,
                    child.parent_id,
                    child.url,
                    child.order_no,
                    child.status,
                    child.last_chg_date,
                    child.app_sequence_no
                FROM mas_application child
                INNER JOIN app_tree at
                    ON child.parent_id = at.app_id
            )
            SELECT
                at.app_id AS appId,
                at.name AS name,
                at.parent_id AS parentId,
                at.url AS url,
                at.order_no AS orderNo,
                at.status AS status,
                at.last_chg_date AS lastChgDate,
                at.app_sequence_no AS appSequenceNo
            FROM app_tree at
            ORDER BY
                COALESCE(at.parent_id, ''),
                at.order_no NULLS LAST,
                at.app_sequence_no NULLS LAST,
                at.app_id
            """, nativeQuery = true)
    List<MasApplicationProjection> findApplicationByParentId(@Param("parentId") String parentId);
}
