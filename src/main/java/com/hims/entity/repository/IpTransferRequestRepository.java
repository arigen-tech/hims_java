package com.hims.entity.repository;

import com.hims.entity.IpTransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpTransferRequestRepository extends JpaRepository<IpTransferRequest,Long> {
}
