package com.hims.entity.repository;

import com.hims.entity.OtChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OtChecklistItemRepository extends JpaRepository<OtChecklistItem, Long> {
    List<OtChecklistItem> findByOtChecklist_OtChecklistIdOrderByOtChecklistItemId(Long otChecklistId);
    void deleteByOtChecklist_OtChecklistId(Long otChecklistId);
}
