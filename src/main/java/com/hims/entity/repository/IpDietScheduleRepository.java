package com.hims.entity.repository;

import com.hims.entity.IpDietSchedule;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpDietScheduleRepository extends JpaRepository<IpDietSchedule,Long> {
}
