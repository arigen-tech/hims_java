package com.hims.entity;
import com.hims.response.ApiResponse;
import com.hims.response.IpdServiceCategoryResponse;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "mas_ipd_service_category", schema = "public")
public class MasIpdServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_subcategory_required")
    private String isSubcategoryRequired;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "gst_applicable", length = 1)
    private String gstApplicable;

    @Column(name = "gst_percentage", precision = 5, scale = 2)
    private BigDecimal gstPercentage;

    // 🔁 Getters and Setters

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getIsSubcategoryRequired() {
        return isSubcategoryRequired;
    }

    public void setIsSubcategoryRequired(String isSubcategoryRequired) {
        this.isSubcategoryRequired = isSubcategoryRequired;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastChgBy() {
        return lastChgBy;
    }

    public void setLastChgBy(String lastChgBy) {
        this.lastChgBy = lastChgBy;
    }

    public LocalDateTime getLastChgDate() {
        return lastChgDate;
    }

    public void setLastChgDate(LocalDateTime lastChgDate) {
        this.lastChgDate = lastChgDate;
    }

    public String getGstApplicable() {
        return gstApplicable;
    }

    public void setGstApplicable(String gstApplicable) {
        this.gstApplicable = gstApplicable;
    }

    public BigDecimal getGstPercentage() {
        return gstPercentage;
    }

    public void setGstPercentage(BigDecimal gstPercentage) {
        this.gstPercentage = gstPercentage;
    }


}