package com.hims.utils;

import com.hims.entity.Inpatient;
import com.hims.entity.IpdBillingDetails;
import com.hims.entity.IpdBillingHeader;
import com.hims.entity.MasIpdServiceCategory;
import com.hims.entity.repository.IpdBillingDetailsRepository;
import com.hims.entity.repository.IpdBillingHeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Slf4j
@Component
@RequiredArgsConstructor
public class SaveIpdBillingDetails {

@Autowired
IpdBillingHeaderRepository ipdBillingHeaderRepository;
@Autowired
IpdBillingDetailsRepository ipdBillingDetailsRepository;

    public void saveDailyCaseSheetBillingDetails(Inpatient inpatient,
                                                  BigDecimal rate,
                                                  BigDecimal quantity,
                                                  BigDecimal gstPercent,
                                                  BigDecimal discountAmount,
                                                  BigDecimal amount ,
                                                  BigDecimal gstAmount,
                                                  BigDecimal netAmount,
                                                  MasIpdServiceCategory ipdServiceCategoryId,
                                                  String itemName) {

        IpdBillingHeader billingHeader = ipdBillingHeaderRepository.findByInpatientId_InpatientId(inpatient.getInpatientId())
                .orElseThrow(() -> new RuntimeException("IPD billing header not found for inpatient ID: " + inpatient.getInpatientId()));

        IpdBillingDetails billingDetails = new IpdBillingDetails();

        billingDetails.setBillHeader(billingHeader);
        billingDetails.setCategory(ipdServiceCategoryId);
        billingDetails.setItemName(itemName);
        billingDetails.setServiceDate(LocalDateTime.now());
        billingDetails.setQuantity(quantity);
        billingDetails.setRate(rate);
        billingDetails.setAmount(amount);
        billingDetails.setGstPercent(gstPercent);
        billingDetails.setGstAmount(gstAmount);
        billingDetails.setDiscountAmount(discountAmount);
        billingDetails.setNetAmount(netAmount);
        billingDetails.setCreatedAt(LocalDateTime.now());

        IpdBillingDetails savedBillingDetails = ipdBillingDetailsRepository.save(billingDetails);

        updateIpdBillingHeaderAmount(billingHeader, amount, gstAmount, discountAmount, netAmount);

        log.info(
                "Daily case sheet billing saved successfully. billItemId: {}, " + "inpatientId: {}, rate: {}, GST percentage: {}, " +
                        "GST amount: {}, net amount: {}",
                savedBillingDetails.getBillItemId(),
                inpatient.getInpatientId(),
                rate,
                gstPercent,
                gstAmount,
                netAmount);


    }
    private void updateIpdBillingHeaderAmount(IpdBillingHeader billingHeader, BigDecimal amount,BigDecimal gstAmount,   BigDecimal discountAmount, BigDecimal netAmount) {

        BigDecimal currentTotalAmount = billingHeader.getTotalAmount() != null ? billingHeader.getTotalAmount() : BigDecimal.ZERO;

        BigDecimal currentGstAmount = billingHeader.getGstAmount() != null ? billingHeader.getGstAmount() : BigDecimal.ZERO;

        BigDecimal currentDiscountAmount = billingHeader.getDiscountAmount() != null ? billingHeader.getDiscountAmount() : BigDecimal.ZERO;

        BigDecimal currentNetAmount = billingHeader.getNetAmount() != null ? billingHeader.getNetAmount() : BigDecimal.ZERO;

        billingHeader.setTotalAmount(currentTotalAmount.add(amount != null ? amount : BigDecimal.ZERO));

        billingHeader.setGstAmount(currentGstAmount.add(gstAmount != null ? gstAmount : BigDecimal.ZERO));

        billingHeader.setDiscountAmount(currentDiscountAmount.add(discountAmount != null ? discountAmount : BigDecimal.ZERO));

        billingHeader.setNetAmount(currentNetAmount.add(netAmount != null ? netAmount : BigDecimal.ZERO));

        billingHeader.setUpdatedAt(LocalDateTime.now());

        ipdBillingHeaderRepository.save(billingHeader);

        log.info("IPD billing header updated successfully. billId: {}, " +
                        "totalAmount: {}, gstAmount: {}, discountAmount: {}, " +
                        "netAmount: {}, patientPayableAmount: {}",
                billingHeader.getBillId(),
                billingHeader.getTotalAmount(),
                billingHeader.getGstAmount(),
                billingHeader.getDiscountAmount(),
                billingHeader.getNetAmount(),
                billingHeader.getPatientPayableAmount()
        );
    }
}
