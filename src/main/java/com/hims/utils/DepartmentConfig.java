package com.hims.utils;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "department")
@Data
public class DepartmentConfig {
    private List<Long> fixedDepartments;
    private Type type = new Type();
    private StockExpiry stockExpiry = new StockExpiry();

    @Data
    public static class Type {
        private Long store;
        private Long dispensary;
        private Long ward;
        private Long general;
    }

    @Data
    public static class StockExpiry {
        private Integer store;
        private Integer dispensary;
        private Integer ward;
        private Integer general;
    }
}
