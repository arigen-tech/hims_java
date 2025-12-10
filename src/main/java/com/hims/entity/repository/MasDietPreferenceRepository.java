package com.hims.entity.repository;

import com.hims.entity.MasDietPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasDietPreferenceRepository extends JpaRepository<MasDietPreference,Long> {
    List<MasDietPreference> findByStatusIgnoreCase(String y);
}
