package com.hims.entity.repository;

import com.hims.entity.IpProcedureTxn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpProcedureTxnRepository extends JpaRepository<IpProcedureTxn, Long> {


}