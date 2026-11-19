package com.hims.entity.repository;

import com.hims.entity.OtChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface OtChecklistRepository extends JpaRepository<OtChecklist, Long> {
    Optional<OtChecklist> findByOtBooking_OtBookingIdAndChecklistStage(Long otBookingId, String checklistStage);
    List<OtChecklist> findByOtBooking_OtBookingIdAndStatusOrderByChecklistStageAsc(Long otBookingId, String status);
}
