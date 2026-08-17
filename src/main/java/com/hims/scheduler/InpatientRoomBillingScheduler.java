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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InpatientRoomBillingScheduler {

    private final InpatientRepository inpatientRepository;
    private final MasWardRoomTariffRepo masWardRoomTariffRepo;
    private final MasIpdServiceCategoryRepository masIpdServiceCategoryRepository;
    private final SaveIpdBillingDetails saveIpdBillingDetails;

    @Value("${ipd.admission.status.admitted}")
    private Long admittedStatusId;

    @Value("${ipd.service.category.room.rent}")
    private Long ipdServiceCategoryRoomRent;

  @Scheduled(cron = "${ipd.room.billing.scheduler.cron:0 0 23 * * *}")
    //@Scheduled(fixedRateString = "${ipd.room.billing.scheduler.fixed-rate-ms:300000}")
   @Transactional
   public void saveDailyInpatientRoomBilling() {
       log.info("Daily inpatient room billing scheduler started");

       MasIpdServiceCategory billingCategory = masIpdServiceCategoryRepository.findById(ipdServiceCategoryRoomRent)
               .orElseThrow(() -> new RuntimeException("IPD service category not found with ID: " + ipdServiceCategoryRoomRent));

       List<Inpatient> inpatients = inpatientRepository.findAdmittedInpatients(admittedStatusId);
       LocalDate billingDate = LocalDate.now();

       // Step 1: distinct roomIds
       List<Long> roomIds = inpatients.stream().map(ip -> ip.getRoom().getRoomId()).distinct().toList();

       List<MasWardRoomTariff> tariffList = masWardRoomTariffRepo.findCurrentTariffsForRooms(roomIds, billingDate, AppConstants.STATUS_Y.toLowerCase());

       // Step 3: roomId -> tariff
       Map<Long, MasWardRoomTariff> roomTariffMap = tariffList.stream()
               .collect(Collectors.toMap(MasWardRoomTariff::getRoomId, t -> t, (existing, replacement) -> existing));


       for (Inpatient inpatient : inpatients) {
           try {
               Long roomId = inpatient.getRoom().getRoomId();
               MasWardRoomTariff tariff = roomTariffMap.get(roomId);

               if (tariff == null) {
                   log.warn("Skipping room billing for inpatientId={} because tariff is not configured for roomId={}", inpatient.getInpatientId(), roomId);
                   continue;
               }

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
           } catch (Exception e) {
               log.error("Failed room billing for inpatientId={}", inpatient.getInpatientId(), e);
               throw e;
           }
       }

       log.info("Daily inpatient room billing scheduler completed. eligibleCount={}", inpatients.size());
   }
}
