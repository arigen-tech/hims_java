package com.hims.entity.repository;



import com.hims.entity.MasFrequency;
import com.hims.entity.MasGender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasFrequencyRepository extends JpaRepository<MasFrequency,Long> {

    List<MasFrequency> findByStatusIgnoreCase(String y);



    List<MasFrequency> findByStatusInIgnoreCase(List<String> y);
}

