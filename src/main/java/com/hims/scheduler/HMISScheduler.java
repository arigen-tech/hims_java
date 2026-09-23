package com.hims.scheduler;

import com.hims.constants.AppConstants;
import com.hims.entity.Inpatient;
import com.hims.entity.MasIpdServiceCategory;
import com.hims.entity.MasWardRoomTariff;
import com.hims.entity.PaymentRefund;
import com.hims.entity.repository.InpatientRepository;
import com.hims.entity.repository.MasIpdServiceCategoryRepository;
import com.hims.entity.repository.MasWardRoomTariffRepo;
import com.hims.entity.repository.PaymentRefundRepository;
import com.hims.utils.SaveIpdBillingDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
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
public class HMISScheduler {

    private final InpatientRepository inpatientRepository;
    private final MasWardRoomTariffRepo masWardRoomTariffRepo;
    private final MasIpdServiceCategoryRepository masIpdServiceCategoryRepository;
    private final SaveIpdBillingDetails saveIpdBillingDetails;
    private final PaymentRefundRepository paymentRefundRepository;
    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ipd.admission.status.admitted}")
    private Long admittedStatusId;

    @Value("${ipd.service.category.room.rent}")
    private Long ipdServiceCategoryRoomRent;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private static final long ROOM_BILLING_LOCK_KEY = 927341L;
    private static final long REFUND_REFERENCE_LOCK_KEY = 927342L;

    private static final Long GATEWAY_REFUND_MODE_ID = 1L;

    // =========================================================================
    // Daily inpatient room billing
    // =========================================================================

    @Scheduled(cron = "${ipd.room.billing.scheduler.cron:0 0 23 * * *}")
    // @Scheduled(fixedRateString = "${ipd.room.billing.scheduler.fixed-rate-ms:300000}")
    public void saveDailyInpatientRoomBilling() {
        if (!tryAcquireLock(ROOM_BILLING_LOCK_KEY)) {
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


    // Refund gateway reference (RRN/ARN/UTC) sync
    @Scheduled(cron = "${refund.reference.scheduler.cron:0 0/30 * * * *}", zone = "Asia/Kolkata")
    public void syncGatewayRefundReferences() {
        if (!tryAcquireLock(REFUND_REFERENCE_LOCK_KEY)) {
            log.info("Refund reference sync job is already running on another instance. Skipping current node.");
            return;
        }
        log.info("Refund reference (RRN/ARN/UTC) sync scheduler started");

        List<PaymentRefund> pendingRefunds = paymentRefundRepository
                .findByPaymentGatewayIdAndGatewayReferenceNoIsNullAndGatewayReferenceTypeIsNull(GATEWAY_REFUND_MODE_ID);

        if (pendingRefunds.isEmpty()) {
            log.info("No pending refunds found needing gateway reference sync.");
            return;
        }

        int successCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (PaymentRefund refund : pendingRefunds) {
            if (refund.getGatewayRefundId() == null || refund.getGatewayRefundId().isBlank()) {
                log.warn("Skipping refund reference sync because gatewayRefundId is missing. refundId={}", refund.getRefundId());
                skippedCount++;
                continue;
            }
            try {
                boolean updated = syncOneRefund(refund);
                if (updated) {
                    successCount++;
                } else {
                    skippedCount++;
                }
            } catch (Exception e) {
                failedCount++;
                log.error("Failed to sync gateway reference for refundId={}, gatewayRefundId={}",
                        refund.getRefundId(), refund.getGatewayRefundId(), e);
            }
        }
        log.info("Refund reference sync completed. eligibleCount={}, successCount={}, skippedCount={}, failedCount={}",
                pendingRefunds.size(), successCount, skippedCount, failedCount);
    }

    /**
     * Fetches one refund from Razorpay and, if a reference is available yet,
     * updates gateway_reference_no / gateway_reference_type in its own
     * independent transaction. Returns true if a reference was written.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean syncOneRefund(PaymentRefund refund) {
        Map<String, Object> body = fetchRefundFromRazorpay(refund.getGatewayRefundId());
        if (body == null) {
            log.warn("Empty response from Razorpay for gatewayRefundId={}", refund.getGatewayRefundId());
            return false;
        }

        Object acquirerDataObj = body.get("acquirer_data");
        if (!(acquirerDataObj instanceof Map)) {
            log.info("No acquirer_data present yet for gatewayRefundId={}", refund.getGatewayRefundId());
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> acquirerData = (Map<String, Object>) acquirerDataObj;

        String referenceNo = null;
        String referenceType = null;


        if (acquirerData.get("rrn") != null) {
            referenceNo = String.valueOf(acquirerData.get("rrn"));
            referenceType = "RRN";
        } else if (acquirerData.get("arn") != null) {
            referenceNo = String.valueOf(acquirerData.get("arn"));
            referenceType = "ARN";
        } else if (acquirerData.get("utr") != null) {
            referenceNo = String.valueOf(acquirerData.get("utr"));
            referenceType = "UTR";
        }

        if (referenceNo == null) {
            log.info("Razorpay has not returned an RRN/ARN/UTC yet for gatewayRefundId={}", refund.getGatewayRefundId());
            return false;
        }

        refund.setGatewayReferenceNo(referenceNo);
        refund.setGatewayReferenceType(referenceType);
        paymentRefundRepository.save(refund);
        log.info("Updated refundId={} with gatewayReferenceType={}, gatewayReferenceNo={}",
                refund.getRefundId(), referenceType, referenceNo);
        return true;
    }

    private Map<String, Object> fetchRefundFromRazorpay(String gatewayRefundId) {
        String url = "https://api.razorpay.com/v1/refunds/" + gatewayRefundId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(razorpayKeyId, razorpayKeySecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        return response.getBody();
    }

    // Shared cluster-wide advisory lock

    /**
     * Acquires a cluster-wide advisory lock, keyed per job, in its own short
     * transaction. pg_try_advisory_xact_lock auto-releases when this
     * transaction ends, so the lock isn't held for the entire (potentially
     * long) job run.
     */
    @Transactional
    public boolean tryAcquireLock(long lockKey) {
        Boolean acquired = jdbcTemplate.queryForObject("SELECT pg_try_advisory_xact_lock(?)", Boolean.class, lockKey);
        return Boolean.TRUE.equals(acquired);
    }
}