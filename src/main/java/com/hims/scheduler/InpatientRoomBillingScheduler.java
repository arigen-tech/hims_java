package com.hims.scheduler;

import com.hims.constants.AppConstants;
import com.hims.entity.Inpatient;
import com.hims.entity.MasIpdServiceCategory;
import com.hims.entity.MasWardRoomTariff;
import com.hims.entity.repository.InpatientRepository;
import com.hims.entity.repository.MasIpdServiceCategoryRepository;
import com.hims.entity.repository.MasWardRoomTariffRepo;
import com.hims.utils.SaveIpdBillingDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InpatientRoomBillingScheduler {

    private final InpatientRepository inpatientRepository;
    private final MasWardRoomTariffRepo masWardRoomTariffRepo;
    private final MasIpdServiceCategoryRepository masIpdServiceCategoryRepository;
    private final SaveIpdBillingDetails saveIpdBillingDetails;
    private final JdbcTemplate jdbcTemplate;

    @Value("${ipd.admission.status.admitted}")
    private Long admittedStatusId;

    @Value("${ipd.service.category.room.rent}")
    private Long ipdServiceCategoryRoomRent;

    // Unique lock key
    private static final long ROOM_BILLING_LOCK_KEY = 927341L;

    @Scheduled(cron = "${ipd.room.billing.scheduler.cron:0 0 23 * * *}")
   // @Scheduled(fixedRateString = "${ipd.room.billing.scheduler.fixed-rate-ms:300000}")
    public void saveDailyInpatientRoomBilling() {
        if (!tryAcquireLock()) {
            log.info("Room billing job is already running on another instance. Skipping current node.");
            return;
        }
        log.info("Daily inpatient room billing scheduler started");
        try {
            MasIpdServiceCategory billingCategory = masIpdServiceCategoryRepository.findById(ipdServiceCategoryRoomRent)
                    .orElseThrow(() -> new RuntimeException("IPD service category not found with ID: " + ipdServiceCategoryRoomRent));

            List<Inpatient> inpatients = inpatientRepository.findAdmittedInpatients(admittedStatusId);
            if (inpatients.isEmpty()) {
                log.info("No admitted inpatients found. Room billing skipped.");
                return;
            }
            LocalDate billingDate = LocalDate.now();

            List<Long> roomIds = inpatients.stream()
                    .filter(ip -> ip.getRoom() != null)
                    .map(ip -> ip.getRoom().getRoomId())
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            List<MasWardRoomTariff> tariffList = masWardRoomTariffRepo.findCurrentTariffsForRooms(roomIds, billingDate, AppConstants.STATUS_Y.toLowerCase());

            Map<Long, MasWardRoomTariff> roomTariffMap = tariffList.stream().collect(Collectors.toMap(
                            MasWardRoomTariff::getRoomId, t -> t,
                            (existing, replacement) -> {return existing;}));

            int successCount = 0;
            int skippedCount = 0;
            int failedCount = 0;

            for (Inpatient inpatient : inpatients) {
                if (inpatient.getRoom() == null) {
                    log.warn("Skipping billing because room is not assigned. inpatientId={}", inpatient.getInpatientId());
                    skippedCount++;
                    continue;
                }
                Long roomId = inpatient.getRoom().getRoomId();
                MasWardRoomTariff tariff = roomTariffMap.get(roomId);
                if (tariff == null) {
                    log.warn("Skipping room billing because tariff is not configured. inpatientId={}, roomId={}", inpatient.getInpatientId(), roomId);
                    skippedCount++;
                    continue;
                }
                try {
                    // Each call runs in its own committed/rolled-back transaction.
                    billOnePatient(inpatient, tariff, billingCategory);
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    log.error("Failed room billing for inpatientId={}", inpatient.getInpatientId(), e);
                    // no rethrow move on to next patient
                }
            }
            log.info("Daily inpatient room billing completed. eligibleCount={}, successCount={}, skippedCount={}, failedCount={}",
                    inpatients.size(), successCount, skippedCount, failedCount);
        } catch (Exception e) {
            log.error("Unexpected error in daily inpatient room billing scheduler", e);
            throw e;
        }
    }

    /**
     * Acquires the cluster-wide advisory lock in its own short transaction.
     * pg_try_advisory_xact_lock auto-releases when this transaction ends,
     * so the lock isn't held for the entire (potentially long) billing run.
     */
    @Transactional
    public boolean tryAcquireLock() {
        Boolean acquired = jdbcTemplate.queryForObject("SELECT pg_try_advisory_xact_lock(?)", Boolean.class, ROOM_BILLING_LOCK_KEY);
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * Bills exactly one inpatient in its own independent transaction.
     * If this fails (constraint violation, stale data, etc.), only THIS
     * transaction rolls back — billing already committed for other
     * patients (before or after this one in the loop) is unaffected.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void billOnePatient(Inpatient inpatient,
                               MasWardRoomTariff tariff,
                               MasIpdServiceCategory billingCategory) {

        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal rate = tariff.getTariff();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal amount = rate.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gstPercent = billingCategory.getGstPercentage() != null ? billingCategory.getGstPercentage() : BigDecimal.ZERO;
        BigDecimal gstAmount = amount.multiply(gstPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = amount.add(gstAmount).subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        String itemName = "Room Tariff - " + inpatient.getRoom().getRoomName();

        saveIpdBillingDetails.saveInpatientBillingDetails(
                inpatient,
                rate,
                quantity,
                gstPercent,
                discountAmount,
                amount,
                gstAmount,
                netAmount,
                billingCategory,
                null,
                itemName
        );
    }
}