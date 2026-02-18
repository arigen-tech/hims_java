package com.hims.constants;

public class AppConstants {

    public static final String ENTITY_STORE_INTERNAL_INDENT_M="StoreInternalIndentM";
    public static final String ENTITY_STORE_INTERNAL_INDENT_T="StoreInternalIndentT";
    public static final String M_COLUMN_NAME="status";
    public static final String T_COLUMN_NAME="issue_status";
    public static final String INDENT_APPROVED_AT_REQ_DEPT="A";
    public static final String INDENT_REJECTED_AT_REQ_DEPT="R";
    public static final String INDENT_APPROVED_AT_ISSUED_DEPT="AA";
    public static final String INDENT_REJECTED_AT_ISSUED_DEPT="RR";
    public static final String INDENT_SUBMITTED_AT_REQ_DEPT="Y";
    public static final String INDENT_CREATED_AT_REQ_DEPT="S";
    public static final String INDENT_NOT_ISSUED_AT_ISSUE_DEPT="N";
    public static final String ENTITY_STORE_ISSUE_M="StoreIssueM";
    public static final String INDENT_ISSUED_AT_ISSUE_DEPT="I";
    public static final String COLUMN_NAME="status";
    public static final String ENTITY_STORE_ISSUE_T = "StoreIssueT";
    public static final String STATUS_Y = "Y";
    public static final String ENTITY_STORE_INDENT_RECEIVE_M = "StoreIndentReceiveM";
    public static final String STATUS_R = "R";
    public static final String COLUMN_NAME_IS_RETURN="is_return";
    public static final String STATUS_N ="N" ;


    private AppConstants(){}

/**
 * Application-wide constants for HIMS system.
 * Centralizes all hardcoded strings for consistency and maintainability.
 */
public final class AppConstants {
    
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== Status Constants ====================
    
    /**
     * Active status indicator (lowercase)
     */
    public static final String STATUS_ACTIVE = "y";
    
    /**
     * Inactive status indicator (lowercase)
     */
    public static final String STATUS_INACTIVE = "n";
    
    /**
     * Active status indicator (uppercase)
     */
    public static final String STATUS_ACTIVE_UPPER = "Y";
    
    /**
     * Inactive status indicator (uppercase)
     */
    public static final String STATUS_INACTIVE_UPPER = "N";
    
    /**
     * Completed status
     */
    public static final String STATUS_COMPLETED = "c";
    
    /**
     * Completed status (uppercase)
     */
    public static final String STATUS_COMPLETED_UPPER = "C";
    
    /**
     * Pending status
     */
    public static final String STATUS_PENDING = "p";
    
    /**
     * Pending status (uppercase)
     */
    public static final String STATUS_PENDING_UPPER = "P";
    
    /**
     * Saved/Draft status
     */
    public static final String STATUS_SAVED = "s";
    
    /**
     * Saved/Draft status (uppercase)
     */
    public static final String STATUS_SAVED_UPPER = "S";
    
    // ==================== Flag Constants ====================
    
    /**
     * Flag to retrieve all records (both active and inactive)
     */
    public static final int FLAG_ALL = 0;
    
    /**
     * Flag to retrieve only active records
     */
    public static final int FLAG_ACTIVE_ONLY = 1;
    
    // ==================== Pattern Constants ====================
    
    /**
     * Regex pattern for status validation (y/n case insensitive)
     */
    public static final String STATUS_PATTERN = "y|n|Y|N";
    
    /**
     * Regex pattern for extended status validation
     */
    public static final String EXTENDED_STATUS_PATTERN = "y|n|Y|N|c|C|p|P|s|S";
    
    // ==================== Billing Status Constants ====================
    
    /**
     * Billing completed
     */
    public static final String BILLING_STATUS_COMPLETED = "y";
    
    /**
     * Billing pending
     */
    public static final String BILLING_STATUS_PENDING = "n";
    
    // ==================== Visit Status Constants ====================
    
    /**
     * Visit completed
     */
    public static final String VISIT_STATUS_COMPLETED = "c";
    
    /**
     * Visit pending/new
     */
    public static final String VISIT_STATUS_NEW = "n";
    
    // ==================== Boolean-like Constants ====================
    
    /**
     * Yes indicator (uppercase) - for multi-dose, confidential flags, etc.
     */
    public static final String YES = "Y";
    
    /**
     * No indicator (uppercase)
     */
    public static final String NO = "N";
    
    /**
     * Yes indicator (lowercase)
     */
    public static final String YES_LOWER = "y";
    
    /**
     * No indicator (lowercase)
     */
    public static final String NO_LOWER = "n";
    
    /**
     * Common gender - applicable to both genders
     */
    public static final String GENDER_COMMON = "c";
    
    // ==================== Validation Messages ====================
    
    public static final String MSG_INVALID_FLAG = "Invalid flag value. Use 0 for all records or 1 for active records only.";
    public static final String MSG_INVALID_STATUS = "Invalid status value. Must be 'y' or 'n'.";
    public static final String MSG_NOT_FOUND = "Record not found";
    public static final String MSG_CURRENT_USER_NOT_FOUND = "Current user not found";
    public static final String MSG_INTERNAL_ERROR = "Internal Server Error";

}
