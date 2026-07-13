package com.hims.entity.repository;

import com.hims.entity.IpPaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpPaymentDetailRepository extends JpaRepository<IpPaymentDetail,Long> {
}
