package com.hims.entity.repository;

import com.hims.entity.MasAnaesthesiaInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasAnaesthesiaInstructionRepository extends JpaRepository<MasAnaesthesiaInstruction, Long> {

    List<MasAnaesthesiaInstruction> findByStatusIgnoreCaseOrderByInstructionTypeAscInstructionAsc(String status);

    List<MasAnaesthesiaInstruction> findAllByOrderByStatusDescLastChgDateDesc();
}
