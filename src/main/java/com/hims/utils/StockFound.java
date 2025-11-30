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

        if (itemId == null || departmentId == null || noOfDays == null || hospitalId == null) {
            return null;
        }

        // Fetch item
        Optional<MasStoreItem> itemOpt = masStoreItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            return null;
        }
        MasStoreItem itemEntity = itemOpt.get();

        // Fetch all stocks for the item
        List<StoreItemBatchStock> stockList =
                storeItemBatchStockRepository.findByItemId(itemEntity);

        if (stockList == null || stockList.isEmpty()) {
            return null;
        }

        // threshold for expiry filtering
        LocalDate threshold = LocalDate.now().plusDays(noOfDays);

        // Apply hospital, department, expiry and closingStock > 0 filtering
        long totalClosingStock = stockList.stream()
                .filter(s ->
                        s.getHospitalId() != null &&
                                s.getHospitalId().getId().equals(hospitalId) &&

                                s.getDepartmentId() != null &&
                                s.getDepartmentId().getId().equals(departmentId) &&

                                s.getClosingStock() != null &&
                                s.getClosingStock() > 0 &&

                                s.getExpiryDate() != null &&
                                s.getExpiryDate().isAfter(threshold)
                )
                .mapToLong(s -> s.getClosingStock())
                .sum();

        return totalClosingStock;
    }

}
