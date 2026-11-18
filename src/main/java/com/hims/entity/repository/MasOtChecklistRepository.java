package com.hims.entity.repository;

import com.hims.entity.MasOtChecklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface MasOtChecklistRepository extends JpaRepository<MasOtChecklist, Long> {
    Optional<MasOtChecklist> findByChecklistCodeIgnoreCase(String checklistCode);

    @Query(value = """
            SELECT *
            FROM public.mas_ot_checklist
            WHERE UPPER(checklist_stage) = UPPER(:checklistStage)
              AND UPPER(status) = UPPER(:status)
            ORDER BY display_order ASC, checklist_id ASC
            """, nativeQuery = true)
    List<MasOtChecklist> findActiveByStageOrderByDisplayOrder(
            @Param("checklistStage") String checklistStage,
            @Param("status") String status);


    @Query("""
    SELECT c FROM MasOtChecklist c
    WHERE (:flag = 0 OR c.status = 'Y')
      AND (
          COALESCE(CAST(:stage AS string), '') = ''
          OR LOWER(c.checklistStage) LIKE
             LOWER(CONCAT('%', CAST(:stage AS string), '%'))
      )
      AND (
          COALESCE(CAST(:name AS string), '') = ''
          OR LOWER(c.checklistName) LIKE
             LOWER(CONCAT('%', CAST(:name AS string), '%'))
      )
    ORDER BY c.checklistStage, c.displayOrder, c.checklistId
    """)
    Page<MasOtChecklist> search(
            @Param("flag") int flag,
            @Param("stage") String stage,
            @Param("name") String name,
            Pageable pageable
    );

    @Query("""
    SELECT c FROM MasOtChecklist c
    WHERE (:flag = 0 OR c.status = 'Y')
      AND (COALESCE(CAST(:stage AS string), '') = '' OR LOWER(c.checklistStage) LIKE LOWER(CONCAT('%', CAST(:stage AS string), '%')))
      AND (COALESCE(CAST(:name AS string), '') = '' OR LOWER(c.checklistName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
    ORDER BY c.checklistStage, c.displayOrder, c.checklistId
    """)
    List<MasOtChecklist> searchAll(@Param("flag") int flag, @Param("stage") String stage,
                                   @Param("name") String name);

}
