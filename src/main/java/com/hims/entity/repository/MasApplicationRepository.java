package com.hims.entity.repository;

import com.hims.entity.MasApplication;
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
}
