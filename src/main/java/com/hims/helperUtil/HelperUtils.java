package com.hims.helperUtil;

import com.hims.constants.AppConstants;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.MasInvestigationPriceDetails;
import com.hims.entity.MasServiceCategory;
import com.hims.entity.MasSubChargeCode;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.LabInvestigationReq;
import com.hims.request.LabRadioInvestigationRequest;
import com.hims.utils.RandomNumGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class HelperUtils {


    @Autowired
    public RandomNumGenerator randomNumGenerator;

    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepository;

    @Autowired
    private MasSubChargeCodeRepository subChargeCodeRepository;

    @Autowired
    private VisitRepository visitRepository;
    private MasServiceCategory masServiceCategory;

    @Autowired
    private MasServiceCategoryRepository masServiceCategoryRepository;

    @Autowired
    private MasInvestigationPriceDetailsRepository masInvestigationPriceDetailsRepository;

    @Value("${serviceCategoryLab}")
    private String serviceCategoryLab;


    // FOR dev  D:\BmsBackend\webapps\bmsreport
    public static String LASTFOLDERPATH = "D:/payroll/webapps/bmsreport";
    public static String FILEPATH = "https://icg.net.in/bmsreport/";

//     For UAT
//    public static String LASTFOLDERPATH = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/bmsreport";
//    public static String FILEPATH = "https://icg.net.in/bmsreport/";

//     For PROD
//    public static String LASTFOLDERPATH = "C:/Program Files/Tomcat 9.0/webapps/cgbmsreport";
//    public static String FILEPATH = "https://icg.net.in/cgbmsreport/";

    public static String getRollId() {
        return "ROL_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static Timestamp getCurrentTimeStamp() {
        return new Timestamp(new Date().getTime());
    }

    public static String getOtp() {
        return ConverterUtils.generateOTP();
    }


    public  String sendSMS(String mobile, String name,String password) {
        try {
            final String uri ="https://2factor.in/API/R1/?module=TRANS_SMS&apikey=5cdc6365-22b5-11ec-a13b-0200cd936042&to="+mobile+
                    "&from=CGMMSY&templatename=Username-New&var1="+name+"&var2="+mobile+"&var3="+password;

            MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<String, String>();
            RestTemplate restTemplate = new RestTemplate();
            String responseObject = restTemplate.postForObject(uri, requestHeaders, String.class);

            log.info("Response from SMS API: {}", responseObject);
            log.info("SMS sent successfully");
            return responseObject;
        } catch (Exception e) {
            log.error("Error occurred while sending SMS", e);
            return ResponseUtils.getReturnMsg("0", "We are unable to process your request");
        }
    }

    public  String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public static String extractTimeFromInstant(Instant instant) {
        return instant.atZone(ZoneId.of("Asia/Kolkata"))
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }


    public static String instantTimeToLocalDateTime(Instant instant) {
        return instant.atZone(ZoneId.of("Asia/Kolkata"))
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String createInvoices() {
        return randomNumGenerator.generateOrderNumber("BILL",true,true);
    }

    public String createInvoiceNumber() {
        return randomNumGenerator.generateOrderNumber("HIMS", true, true);
    }

    public void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Page size must be greater than zero"
            );
        }
        if (size > 100) {
            throw new IllegalArgumentException(
                    "Page size cannot be greater than 100"
            );
        }
    }

    public void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return;
        }
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "Both from date and to date are required when using date filter"
            );
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "From date cannot be greater than to date"
            );
        }
    }


    public String cleanValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    public String normalizeRefundStatusFilter(String value) {
        String cleaned = cleanValue(value);
        if (cleaned == null) {
            return null;
        }

        if ("completed".equalsIgnoreCase(cleaned) || AppConstants.STATUS_Y.equalsIgnoreCase(cleaned)) {
            return AppConstants.STATUS_Y.toLowerCase();
        }

        if ("pending".equalsIgnoreCase(cleaned) || AppConstants.STATUS_N.equalsIgnoreCase(cleaned)) {
            return AppConstants.STATUS_N.toLowerCase();
        }

        if ("all".equalsIgnoreCase(cleaned)) {
            return null;
        }

        return cleaned.toUpperCase();
    }

    public Long getDepartmentFromInvestigation(Long investigationId) {
        if (investigationId == null) {
            throw new SDDException("investigation", 400, "Investigation ID cannot be null");
        }

        DgMasInvestigation investigation = dgMasInvestigationRepository.findById(investigationId)
                .orElseThrow(() -> new SDDException("investigation", 404, "Investigation not found with ID: " + investigationId));

        if (investigation.getSubChargeCodeId() == null) {
            throw new SDDException("subcharge", 400, "Subcharge code not configured for investigation ID: " + investigationId);
        }

        Long subChargeId = investigation.getSubChargeCodeId().getSubId();
        if (subChargeId == null) {
            throw new SDDException("subcharge", 400, "SubCharge ID is null for investigation ID: " + investigationId);
        }

        MasSubChargeCode subChargeCode = subChargeCodeRepository.findById(subChargeId)
                .orElseThrow(() -> new SDDException("subcharge", 404, "Subcharge code not found for investigation ID: " + investigationId));

        if (subChargeCode.getMasDepartment() == null) {
            throw new SDDException("department", 400, "Department not configured for subcharge code");
        }

        return subChargeCode.getMasDepartment().getId();
    }


    public String getVisitTypeForFollowUpOrNew(Long patientId) {
        int count = visitRepository.countByPatientIdAndVisitDate(patientId);
        return count > 0 ? AppConstants.STATUS_F : AppConstants.STATUS_N;
    }


    public  BigDecimal[] calculateBillingAmounts(List<? extends Object> investigations, String serviceCategory) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal disc = BigDecimal.ZERO;


        masServiceCategory = masServiceCategoryRepository.findByServiceCateCode(serviceCategory);


        for (Object inv : investigations) {
            BigDecimal actualAmount = BigDecimal.ZERO;
            BigDecimal discountedAmount = BigDecimal.ZERO;

            if (inv instanceof LabRadioInvestigationRequest) {
                actualAmount = ((LabRadioInvestigationRequest) inv).getActualAmount();
                discountedAmount = ((LabRadioInvestigationRequest) inv).getDiscountedAmount();
            } else if (inv instanceof LabInvestigationReq) {
                actualAmount = BigDecimal.valueOf(((LabInvestigationReq) inv).getActualAmount());
                discountedAmount = BigDecimal.valueOf(((LabInvestigationReq) inv).getDiscountedAmount());
            }

            sum = sum.add(actualAmount);
            disc = disc.add(discountedAmount);

            if (masServiceCategory != null && masServiceCategory.getGstApplicable()) {
                tax = tax.add(BigDecimal.valueOf(masServiceCategory.getGstPercent())
                        .multiply(actualAmount)
                                .subtract(discountedAmount)
                        .divide(BigDecimal.valueOf(100)));
            }
        }

        return new BigDecimal[]{sum, tax, disc};
    }

    /**
     * Gets the current investigation price from investigation_price_details table by investigation ID
     * Tries multiple strategies:
     * 1. Get active price for current date
     * 2. Get latest price if no active price found
     * 3. Fallback to investigation's direct price field
     *
     * @param investigation DgMasInvestigation entity
     * @return BigDecimal price, or BigDecimal.ZERO if not found
     */
    public BigDecimal getInvestigationPrice(DgMasInvestigation investigation) {
        if (investigation == null) {
            return BigDecimal.ZERO;
        }
        LocalDate today = LocalDate.now();
        Optional<MasInvestigationPriceDetails> priceDetail = masInvestigationPriceDetailsRepository.findActivePriceByInvestigationAndDate(investigation, today);

        if (priceDetail.isPresent() && priceDetail.get().getPrice() != null) {
            log.debug("Found active investigation price for investigation ID: {} - Price: {}", investigation.getInvestigationId(), priceDetail.get().getPrice());
            return priceDetail.get().getPrice();
        }
        Optional<MasInvestigationPriceDetails> latestPrice = masInvestigationPriceDetailsRepository.findTopByInvestigationOrderByFromDateDesc(investigation);
        if (latestPrice.isPresent() && latestPrice.get().getPrice() != null) {
            log.debug("Found latest investigation price for investigation ID: {} - Price: {}", investigation.getInvestigationId(), latestPrice.get().getPrice());
            return latestPrice.get().getPrice();
        }
        if (investigation.getPrice() != null) {
            log.debug("Using fallback investigation direct price for investigation ID: {} - Price: {}", investigation.getInvestigationId(), investigation.getPrice());
            return BigDecimal.valueOf(investigation.getPrice());
        }
        log.warn("No price found for investigation ID: {}", investigation.getInvestigationId());
        return BigDecimal.ZERO;
    }


}
