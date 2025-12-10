package com.hims.entity.repository;

import com.hims.entity.DgOrderHd;
import com.hims.entity.Visit;
import com.hims.response.PendingSampleResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabHdRepository extends JpaRepository<DgOrderHd,Integer> {
    @Query(value = "SELECT MAX(CAST(SUBSTRING(order_no FROM '[0-9]+$') AS INTEGER)) FROM dg_orderhd WHERE order_no ~ '^ord-[0-9]+$'", nativeQuery = true)
    Integer findMaxOrderNo();
    Optional<DgOrderHd> findTopByOrderByIdDesc();

    List<DgOrderHd> findByPaymentStatusIn(List<String> paymentStatuses);

    List<DgOrderHd> findByPaymentStatusInAndOrderStatusIn(List<String> paymentStatuses, List<String> orderStatusFilter);

    List<DgOrderHd> findAllByVisitId(Visit visit);


    @Query("SELECT h FROM DgOrderHd h " +
            "WHERE h.paymentStatus IN :paymentStatuses " +
            "AND h.orderStatus IN :orderStatusFilter " +
            "AND h.appointmentDate BETWEEN :startDate AND :endDate")
    List<DgOrderHd> findPendingOrdersByDateRange(
            List<String> paymentStatuses,
            List<String> orderStatusFilter,
            LocalDate startDate,
            LocalDate endDate
    );




    DgOrderHd findByVisitId(Visit visitId);

    Optional<DgOrderHd> findByPatientId_Id(Long patientId);

    DgOrderHd findById(Long orderHdId);

    List<DgOrderHd> findByPaymentStatusInAndSource(List<String> paymentStatuses, String source);



    @Transactional
    @Modifying
    @Query("UPDATE DgOrderHd h SET h.orderStatus = :status WHERE h.id = :id")
    void updateOrderStatus(Long id, String status);


//    @Query("SELECT h FROM DgOrderHd h WHERE h.patientId.patientId = :patientId AND h.visitId.visitId = :visitId")
//    DgOrderHd findByPatientIdAndVisitId(Long patientId, Long visitId);

//    @Query("SELECT h FROM DgOrderHd h " +
//            "WHERE h.patientId.id = :patientId  AND h.visitId.id = :visitId")
//    DgOrderHd findByPatientIdAndVisitId(Long patientId, Long visitId);

    DgOrderHd findByPatientId_IdAndVisitId_Id(Long id, Long id1);



//    Optional<DgOrderHd> findByPatientId_IdAndOrderStatus(Long patientId, String n);
}



