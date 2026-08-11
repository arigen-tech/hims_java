package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchNameForStockResponse {

    private Long stockId;
    private String batchName;
    private LocalDate dom;
    private LocalDate doe;
    private Long batchStock;
    private Long availableStock;
    private Long manufacturerId;
}
