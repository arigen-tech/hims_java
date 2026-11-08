package com.hims.entity.repository;

import com.hims.entity.MasOperationTheatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MasOperationTheatreRepository extends JpaRepository<MasOperationTheatre,Long> {
    @Query(value = "SELECT ot_id FROM mas_operation_theatre WHERE ot_id = :otId AND UPPER(status) = 'Y'", nativeQuery = true)
    java.util.Optional<Long> findActiveOtId(@Param("otId") Long otId);
    List<MasOperationTheatre> findByStatusIgnoreCaseOrderByOtNameAsc(String lowerCase);

    List<MasOperationTheatre> findAllByOrderByStatusDescLastChgDateDesc();
}
