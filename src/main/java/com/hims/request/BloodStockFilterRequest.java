package com.hims.request;

import lombok.Data;

@Data
public class BloodStockFilterRequest {
    private Long bloodGroupId;
    private Long componentId;
    private Long inventoryStatus; // AVAILABLE / RESERVED / ISSUED
    private String expiryFilter; // "24HRS", "3DAYS", "7DAYS"
   private Long collectionType; // optional
    private String viewType;
    // "SUMMARY" or "DETAILED"
    private Long hospitalId;
}