package com.hims.entity.repository;

import com.hims.entity.MasWardRoomTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MasWardRoomTariffRepo extends JpaRepository<MasWardRoomTariff, Long> {

    List<MasWardRoomTariff> findByStatusIgnoreCaseOrderByEffectiveFromDesc(String status);

    List<MasWardRoomTariff> findAllByOrderByStatusDescLastUpdatedDateDesc();

    // Check for overlapping tariffs (only active ones)
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM MasWardRoomTariff t " +
            "WHERE t.ward.wardId = :wardId " +
            "AND t.room.roomId = :roomId " +
            "AND t.status = 'y' " +
            "AND t.effectiveFrom <= :effectiveTo " +
            "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :effectiveFrom)")
    boolean existsOverlappingTariff(@Param("wardId") Long wardId,
                                    @Param("roomId") Long roomId,
                                    @Param("effectiveFrom") LocalDate effectiveFrom,
                                    @Param("effectiveTo") LocalDate effectiveTo);

    // Find active tariffs for a specific ward and room
    List<MasWardRoomTariff> findByWard_WardIdAndRoom_RoomIdAndStatusIgnoreCaseOrderByEffectiveFromDesc(
            Long wardId, Long roomId, String status);
}