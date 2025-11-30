package com.hims.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hos.define")
@Data
public class DepartmentConfig {

    private Long storeId;
    private Long dispensaryId;
    private Long wardPharmacyId;
    private Long generalMedicineId;

    private StockExpiry stockExpiry = new StockExpiry();

    @Data
    public static class StockExpiry {
        private Integer store;       // e.g. hos.define.stock-expiry.store=...
        private Integer dispensary;  // e.g. hos.define.stock-expiry.dispensary=...
        private Integer ward;        // e.g. hos.define.stock-expiry.ward=...
        private Integer general;     // e.g. hos.define.stock-expiry.general=...
    }
}
