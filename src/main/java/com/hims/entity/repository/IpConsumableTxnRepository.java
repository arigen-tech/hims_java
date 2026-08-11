package com.hims.entity.repository;

import com.hims.entity.IpConsumableTxn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpConsumableTxnRepository extends JpaRepository<IpConsumableTxn,Long> {
}
