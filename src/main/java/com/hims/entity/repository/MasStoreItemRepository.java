package com.hims.entity.repository;

import com.hims.entity.MasItemCategory;
import com.hims.entity.MasStoreItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasStoreItemRepository extends JpaRepository<MasStoreItem,Long> {
  //  List<MasStoreItem> findByStatusIgnoreCaseAndHospitalIdAndDepartmentId(String y, Long hospitalId, Long departmentId);

   // List<MasStoreItem> findByStatusInIgnoreCaseAndHospitalIdAndDepartmentId(List<String> y,  Long hospitalId, Long departmentId);

    Optional<MasStoreItem> findByPvmsNo(String code);

    List<MasStoreItem> findByStatusIgnoreCaseAndSectionId_SectionId(String status, Integer sectionId);

    List<MasStoreItem> findByStatusInIgnoreCaseAndSectionId_SectionId(List<String> statuses, Integer sectionId);



    Optional<MasStoreItem> findFirstByPvmsNoOrNomenclature(String pvmsNo, String nomenclature);

   
    Optional<MasStoreItem> findByPvmsNoAndItemIdNot(String pvmsNo, Long id);

    Optional<MasStoreItem> findByNomenclatureAndItemIdNot(String nomenclature, Long id);

    List<MasStoreItem> findByStatus(String y);
    List<MasStoreItem> findByStatusIgnoreCase(String y);
    List<MasStoreItem> findByStatusInIgnoreCase(List<String> y);

    List<MasStoreItem> findByStatusIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(String status);

    List<MasStoreItem> findByStatusInIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(List<String> statuses);


    List<MasStoreItem> findByStatusIgnoreCaseOrderByNomenclatureAsc(String y);

  //  List<MasStoreItem> findByStatusIgnoreCaseInOrderByLastChgDateDesc(List<String> y);

    List<MasStoreItem> findAllByOrderByStatusDescLastChgDateDesc();

    List<MasStoreItem> findByStatusInIgnoreCaseOrderByStatusDescLastChgDateDescLastChgTimeDesc(List<String> y);
    @Query("""
       SELECT m FROM MasStoreItem m
       WHERE LOWER(m.status) IN :status
       ORDER BY m.status DESC,
                m.lastChgDate DESC,
                m.lastChgTime DESC
       """)
    List<MasStoreItem> findAllOrderByStatusDesc(
            @Param("status") List<String> status
    );


    Page<MasStoreItem> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<MasStoreItem> findByStatusInIgnoreCase(List<String> status, Pageable pageable);

    @Query("""
    SELECT m FROM MasStoreItem m
    WHERE
      ((:flag = 0 AND LOWER(m.status) IN ('y','n')) OR (:flag = 1 AND LOWER(m.status) = 'y'))
      AND (:sectionId IS NULL OR m.sectionId.sectionId = :sectionId)
      AND (:search IS NULL OR LOWER(m.nomenclature) LIKE %:search% OR LOWER(m.pvmsNo) LIKE %:search%)
    """)
    Page<MasStoreItem> dynamicSearch(
            @Param("flag") int flag,
            @Param("sectionId") Long sectionId,
            @Param("search") String search,
            Pageable pageable
    );

    Page<MasStoreItem> findByNomenclatureContainingIgnoreCaseAndStatus(
            String nomenclature,
            String status,
            Pageable pageable
    );

    List<MasStoreItem> findByItemIdIn(List<Long> list);
}
