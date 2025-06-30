package com.hims.entity.repository;

import com.hims.entity.OrderSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSequenceRepo extends JpaRepository<OrderSequence,String> {
}
