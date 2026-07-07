package com.hims.entity.repository;

import com.hims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DashboardRepository extends JpaRepository<User, Long> {

    @Query(value = """
            SELECT *
            FROM public.fn_hims_dashboard(:fromDate, :toDate)
            """, nativeQuery = true)
    String getDashboardData(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
            SELECT *
            FROM public.fn_hims_dashboard_billing_finance(:fromDate, :toDate)
            """, nativeQuery = true)
    String getBillingFinanceDashboardData(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
