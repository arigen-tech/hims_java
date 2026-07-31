package com.hims.entity.repository;

import com.hims.entity.IpdBlReceiptHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpdBlReceiptHdRepository extends JpaRepository<IpdBlReceiptHd,Long> {
}
