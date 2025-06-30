package com.hims.response;

import com.hims.request.MasStoreItemRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class MasHsnResponse {

    private String hsnCode;
    private BigDecimal gstRate;
    private Boolean isMedicine;
    private String hsnCategory;
    private String hsnSubcategory;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private String createdBy;
    private LocalDateTime lastUpdatedDt;
}
