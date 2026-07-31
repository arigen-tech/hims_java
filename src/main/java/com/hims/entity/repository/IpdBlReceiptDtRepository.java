package com.hims.entity.repository;

import com.hims.entity.IpdBlReceiptDt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpdBlReceiptDtRepository extends JpaRepository<IpdBlReceiptDt,Long> {
}
