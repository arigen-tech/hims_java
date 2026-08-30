package com.hims.mapper;

import com.hims.projection.IndentDetailsWithAvlStockProjection;
import com.hims.response.IndentDetailsWithAvlStock;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public IndentDetailsWithAvlStock mapToResponseIndentDetailsWithAvlStock(
            IndentDetailsWithAvlStockProjection projection) {

        return new IndentDetailsWithAvlStock(
                projection.getIndentTId(),
                projection.getItemName(),
                projection.getItemUnitName(),
                projection.getQtyRequested(),
                projection.getQtyApproved(),
                projection.getQtyReceived(),
                projection.getReasonForIndent(),
                projection.getAvailableStock()
        );
    }
}
