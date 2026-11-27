package com.hims.mapper;

import com.hims.entity.OtSurgeryDrug;
import com.hims.response.OtSurgeryDrugResponse;
import org.springframework.stereotype.Component;

@Component
public class OtSurgeryDrugMapper {
    public OtSurgeryDrugResponse toResponse(OtSurgeryDrug drug) {
        return OtSurgeryDrugResponse.builder()
                .otSurgeryDrugId(drug.getOtSurgeryDrugId())
                .otSurgeryExecutionId(drug.getOtSurgeryExecution().getOtSurgeryExecutionId())
                .itemId(drug.getItem().getItemId())
                .itemName(drug.getItemName())
                .quantity(drug.getQuantity())
                .batchNo(drug.getBatchNo())
                .remarks(drug.getRemarks())
                .lastChgDate(drug.getLastChgDate())
                .build();
    }
}
