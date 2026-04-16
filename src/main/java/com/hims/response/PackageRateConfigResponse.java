package com.hims.response;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PackageRateConfigResponse {
    private Long configId;
    private Long packageId;
    private String packageName;
    private Long billingTypeId;
    private String billingTypeName;
    private Long insuranceId;
    private String insuranceName;
    private Long tpaId;
    private String tpaName;
    private Long corporateId;
    private String corporateName;
    private Long roomCategoryId;
    private String roomCategoryName;
    private BigDecimal amount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String preAuthRequired;
    private BigDecimal copayPercent;
    private BigDecimal maxClaimAmount;
    private String status;

}
