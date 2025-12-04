package com.hims.utils;

import com.hims.entity.MasStoreItem;
import com.hims.entity.StoreItemBatchStock;
import com.hims.entity.repository.MasStoreItemRepository;
import com.hims.entity.repository.StoreItemBatchStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockFound {
    @Autowired
    private  StoreItemBatchStockRepository storeItemBatchStockRepository;

    @Autowired
    private MasStoreItemRepository masStoreItemRepository;


    public Long getAvailableStocks(Long hospitalId, Integer departmentId, Long itemId, Integer noOfDays) {

        System.out.println("===== DEBUG: getAvailableStocks() START =====");
        System.out.println("Input → hospitalId=" + hospitalId +
                ", departmentId=" + departmentId +
                ", itemId=" + itemId +
                ", noOfDays=" + noOfDays);

        if (hospitalId == null || departmentId == null || itemId == null || noOfDays == null) {
            System.out.println("❌ Invalid input");
            return null;
        }

        Optional<MasStoreItem> itemOpt = masStoreItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            System.out.println("❌ Item NOT found");
            return null;
        }

        MasStoreItem itemEntity = itemOpt.get();
        List<StoreItemBatchStock> stockList = storeItemBatchStockRepository.findByItemId(itemEntity);

        if (stockList == null || stockList.isEmpty()) {
            System.out.println("❌ No stock found");
            return null;
        }

        System.out.println("✔ Total Stock Records Found: " + stockList.size());

        LocalDate threshold = LocalDate.now().plusDays(noOfDays);
        System.out.println("Threshold Expiry Date: " + threshold);

        System.out.println("----- FILTERING START -----");

        long totalClosingStock =
                stockList.stream()
                        .filter(s -> {

                            boolean hospitalMatch = s.getHospitalId() != null &&
                                    String.valueOf(s.getHospitalId().getId())
                                            .equals(String.valueOf(hospitalId));

                            boolean deptMatch = s.getDepartmentId() != null &&
                                    String.valueOf(s.getDepartmentId().getId())
                                            .equals(String.valueOf(departmentId));

                            boolean closingMatch = s.getClosingStock() != null &&
                                    s.getClosingStock() > 0;

                            boolean expiryMatch = s.getExpiryDate() != null &&
                                    !s.getExpiryDate().isBefore(threshold);

                            System.out.println("Checking StockId=" + s.getStockId() +
                                    " -> hospitalMatch=" + hospitalMatch +
                                    ", deptMatch=" + deptMatch +
                                    ", closingMatch=" + closingMatch +
                                    ", expiryMatch=" + expiryMatch);

                            return hospitalMatch && deptMatch && closingMatch && expiryMatch;
                        })
                        .mapToLong(StoreItemBatchStock::getClosingStock)
                        .sum();

        System.out.println("===== FINAL TOTAL = " + totalClosingStock + " =====");
        System.out.println("===== DEBUG END =====");

        return totalClosingStock;
    }


}
