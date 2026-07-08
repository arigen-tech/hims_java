package com.hims.entity.repository;

import com.hims.entity.Inpatient;
import lombok.Locked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InpatientRepository extends JpaRepository<Inpatient,Long> {
}
