package com.hims.utils;

import com.hims.constants.AppConstants;
import com.hims.entity.StoreItemBatchStock;
import com.hims.entity.StoreStockLedger;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.StoreItemBatchStockRepository;
import com.hims.entity.repository.StoreStockLedgerRepository;
import com.hims.request.StoreStockLedgerRequest;
import com.hims.request.UpdateStoreItemBatchStockRequest;
import com.hims.response.StockUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryUtils {

    private final StoreItemBatchStockRepository storeItemBatchStockRepository;

    private final MasDepartmentRepository departmentRepository;

    private final MasHospitalRepository hospitalRepository;

    private final StoreStockLedgerRepository storeStockLedgerRepository;

    public StockUpdateResponse updateStoreItemBatchStock(
            UpdateStoreItemBatchStockRequest request) {

        if (request.getStockId() == null) {
            throw new RuntimeException("Stock ID cannot be null");
        }

        StoreItemBatchStock storeItemBatchStock =
                storeItemBatchStockRepository.findById(request.getStockId())
                        .orElseThrow(() -> new RuntimeException(
                                "Stock not found for ID: " + request.getStockId()
                        ));

        Long qtyOut = request.getOpdIssueQty().longValue();

        Long qtyBefore = storeItemBatchStock.getClosingStock();

        if (qtyOut <= 0) {
            throw new RuntimeException(
                    "Issued quantity must be greater than zero"
            );
        }

        if (qtyBefore < qtyOut) {
            throw new RuntimeException(
                    "Insufficient stock. Available: "
                            + qtyBefore
                            + ", Requested: "
                            + qtyOut
            );
        }

        Long qtyAfter = qtyBefore - qtyOut;

        storeItemBatchStock.setOpdIssueQty(qtyOut);
        storeItemBatchStock.setClosingStock(qtyAfter);
        storeItemBatchStock.setLastChgDate(LocalDateTime.now());
        storeItemBatchStock.setLastChgBy(request.getLastChgBy());

        StoreItemBatchStock savedStock =
                storeItemBatchStockRepository.save(storeItemBatchStock);

        return new StockUpdateResponse(
                savedStock.getStockId(),
                qtyBefore,
                qtyOut,
                qtyAfter
        );
    }


    public Long updateStoreStockLedger(StoreStockLedgerRequest request) {
        return storeStockLedgerRepository.save(mapToStoreStockLedger(request)).getLedgerId();
    }

    private StoreStockLedger mapToStoreStockLedger(StoreStockLedgerRequest request) {
        StoreStockLedger ledger = new StoreStockLedger();
        if(request.getStockId()!=null){
          storeItemBatchStockRepository.findById(request.getStockId()).ifPresent(ledger::setStockId);
        }
        ledger.setTxnType(request.getTxnType());
        ledger.setTxnReferenceId(request.getTxnReferenceId());
        ledger.setQtyIn(request.getQtyIn());
        ledger.setQtyOut(request.getQtyOut());
        ledger.setQtyReject(request.getQtyReject());
        ledger.setRemarks(request.getRemarks());
        ledger.setQtyBefore(request.getQtyBefore());
        ledger.setQtyAfter(request.getQtyAfter());
        ledger.setTxnSource(request.getTxnSource());
        ledger.setReferenceNum(request.getReferenceNo());
        ledger.setTxnDate(LocalDate.now());
        ledger.setCreatedDt(LocalDateTime.now());
        ledger.setCreatedBy(request.getCreatedBy());
        if(request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId()).ifPresent(ledger::setDept);
        }
        if (request.getHospitalId() != null) {
            hospitalRepository.findById(request.getHospitalId()).ifPresent(ledger::setHospital);
        }
        return ledger;
    }

    public String generateIssueNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(AppConstants.DATE_TIME_FORMAT_FOR_RANDOM_NO_GENERATION));
        return AppConstants.ISSUE_NUM_GENERATION_PREFIX + timestamp;
    }
}
