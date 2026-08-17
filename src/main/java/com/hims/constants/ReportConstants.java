package com.hims.constants;

public final class ReportConstants {

    private ReportConstants() {
        // prevent instantiation
    }

    public static final String REPORT_FLAG_DOWNLOAD = "D";
    public static final String REPORT_FLAG_PRINT = "P";
    public static final int HTTP_STATUS_BAD_REQUEST = 400;

    // General Report Error Messages
    public static final String ERROR_FAILED_TO_GENERATE_REPORT = "Failed to generate report: ";
    public static final String ERROR_INVALID_FLAG = "Invalid flag value. Use D or P";


    public static final String JASPER_BASE_PATH_DISPENSARY = "/jasperReport/Dispensary/";
    public static final String JASPER_BASE_PATH_LAB = "/jasperReport/Lab/";
    public static final String JASPER_BASE_PATH_OPD = "/jasperReport/OPD/";
    public static final String JASPER_BASE_PATH_STORE = "/jasperReport/Store/";
    public static final String JASPER_BASE_PATH_RADIOLOGY = "/jasperReport/Radiology/";
    public static final String JASPER_BASE_PATH_REGISTRATION = "/jasperReport/Registration/";
    public static final String JASPER_BASE_PATH_BILLING = "/jasperReport/Billing/";
    public static final String JASPER_BASE_PATH_BLOOD_BANK = "/jasperReport/BloodBank/";
    public static final String JASPER_BASE_PATH_IPD = "/jasperReport/IPD/";

    public static final String IPD_SUB_REPORT_DIR = "Subreports/";

    public static final String ASSET_LOGO = "/Assets/arigen_health.png";

    public static final String LAB_INVESTIGATION_JASPER = "Lab_investigation_report";
    public static final String LAB_INVESTIGATION_REPORT = "LabInvestigationReport";

    public static final String OPD_SUBREPORT_DIR = "opdCaseSheetReport/";
    public static final String OPD_CASESHEET_JASPER = "opd_casesheet_new_report_1";
    public static final String OPD_CASESHEET_REPORT = "OpdCaseSheetReport";

    public static final String OPD_TOKEN_JASPER = "opd_token";
    public static final String OPD_TOKEN_REPORT = "OpdToken";

    public static final String INDENT_JASPER = "Indent_report";
    public static final String INDENT_REPORT = "IndentReport";

    public static final String DRUG_EXPIRY_JASPER = "Drug_Expiry_Report";
    public static final String DRUG_EXPIRY_REPORT = "DrugExpiryReport";

    public static final String STOCK_TAKING_JASPER = "Stock_taking_report";
    public static final String STOCK_TAKING_REPORT = "StockTakingReport";

    public static final String STOCK_TAKING_REGISTER_JASPER = "Stock_Taking_Register";
    public static final String STOCK_TAKING_REGISTER_REPORT = "StockTakingRegister";

    public static final String OPENING_BALANCE_JASPER = "Opening_Balance_report";
    public static final String OPENING_BALANCE_REPORT = "OpeningBalanceReport";

    public static final String OPENING_BALANCE_REGISTRY_JASPER = "Opening_Balance_Register_report";
    public static final String OPENING_BALANCE_REGISTRY_REPORT = "OpeningBalanceRegisterReport";

    public static final String STOCK_STATUS_DETAILED_JASPER = "Stock_Status_Detail";
    public static final String STOCK_STATUS_DETAILED_REPORT = "StockStatusDetail";

    public static final String STOCK_SUMMARY_JASPER="Stock_Status_Summary";
    public static final String STOCK_SUMMARY_REPORT="StockStatusSummary";

    public static final String OPD_JASPER = "opd_biling_maxx";
    public static final String OPD_REPORT = "OpdInvoice";

    public static final String LAB_JASPER = "Lab_report";
    public static final String LAB_REPORT = "LabInvoice";

    public static final String INDENT_MEDICINE_ISSUE_REGISTER_SUBREPORT_DIR = "IndentMedicineIssueRegisterReport/";
    public static final String INDENT_MEDICINE_ISSUE_REGISTER_JASPER = "Medicine_issue_register";
    public static final String INDENT_MEDICINE_ISSUE_REGISTER_REPORT = "IndentMedicineIssueRegisterReport";

    public static final String INDENT_ISSUE_JASPER = "indent_issue_report";
    public static final String INDENT_ISSUE_REPORT = "IndentIssueReport";

    public static final String INDENT_RECEIVING_JASPER = "indent_receive_report";
    public static final String INDENT_RECEIVING_REPORT = "IndentReceiveReport";

    public static final String LAB_REGISTER_SUB_REPORT_DIR = "LabRegisterReport/";
    public static final String LAB_REGISTER_JASPER = "Lab_Main_Report";
    public static final String LAB_REGISTER_REPORT = "LabRegisterReport";

    public static final String ITEM_WISE_RECEIVING_JASPER = "Item_wise_receiving_report";
    public static final String ITEM_WISE_RECEIVING_REPORT = "ItemWiseReceivingReport";

    public static final String DATE_WISE_RECEIVING_JASPER = "Date_wise_receiving_report";
    public static final String DATE_WISE_RECEIVING_REPORT = "DateWiseReceivingReport";

    public static final String INDENT_RETURN_JASPER = "Return_report";
    public static final String INDENT_RETURN_REPORT = "ReturnReport";

    public static final String ITEM_WISE_RETURN_JASPER = "Item_wise_return_register";
    public static final String ITEM_WISE_RETURN_REPORT = "ItemWiseReturnRegister";

    public static final String DATE_WISE_RETURN_JASPER = "Date_wise_return_register";
    public static final String DATE_WISE_RETURN_REPORT = "DateWiseReturnRegister";

    public static final String DETAILED_TAT_JASPER = "Detailed_tat_report";
    public static final String DETAILED_TAT_REPORT = "DetailedTatReport";

    public static final String SUMMARY_TAT_JASPER = "Summary_tat_report";
    public static final String SUMMARY_TAT_REPORT = "SummaryTatReport";

    public static final String RESULT_AMENDMENT_JASPER = "Result_amendment";
    public static final String RESULT_AMENDMENT_REPORT = "ResultAmendmentReport";

    public static final String STOCK_MOVEMENT_JASPER = "Item_stock_moment_history";
    public static final String STOCK_MOVEMENT_REPORT = "ItemStockMomentHistory";

    public static final String RADIOLOGY_JASPER = "radiology_report";
    public static final String RADIOLOGY_REPORT = "RadiologyReport";

    public static final String OPD_REGISTER_JASPER = "OPD_register_report";
    public static final String OPD_REGISTER_REPORT = "OPDRegisterReport";

    public static final String APPOINTMENT_SUMMARY_DEPARTMENT_JASPER = "Appointment_summary_department";
    public static final String APPOINTMENT_SUMMARY_DEPARTMENT_REPORT = "AppointmentSummaryDepartment";

    public static final String APPOINTMENT_SUMMARY_DEPARTMENT_JASPER_DASHED = "Appointment_summary_department_dashed";
    public static final String APPOINTMENT_SUMMARY_DEPARTMENT_REPORT_DASHED = "AppointmentSummaryDepartmentDashed";

    public static final String APPOINTMENT_SUMMARY_DOCTOR_JASPER = "Appointment_summary_doctor";
    public static final String APPOINTMENT_SUMMARY_DOCTOR_REPORT = "AppointmentSummaryDoctor";

    public static final String APPOINTMENT_SUMMARY_DOCTOR_JASPER_DASHED = "Appointment_summary_doctor_dashed";
    public static final String APPOINTMENT_SUMMARY_DOCTOR_REPORT_DASHED = "AppointmentSummaryDoctorDashed";

    public static final String DAILY_CANCELLATION_JASPER = "Daily_cancellation_report";
    public static final String DAILY_CANCELLATION_REPORT = "DailyCancellationReport";

    public static final String RADIOLOGY_INVOICE_JASPER = "Radiology_invoice";
    public static final String RADIOLOGY_INVOICE_REPORT = "RadiologyInvoice";

    public static final String SAMPLE_REJECTION_JASPER = "Sample_rejection_report";
    public static final String SAMPLE_REJECTION_REPORT = "SampleRejectionReport";

    public static final String PENDING_INVESTIGATION_JASPER = "Pending_investigation_report";
    public static final String PENDING_INVESTIGATION_REPORT = "PendingInvestigationReport";

    public static final String OPD_BILLING_REGISTER_JASPER = "Opd_billing_register";
    public static final String OPD_BILLING_REGISTER_REPORT = "OpdBillingRegisterReport";

    public static final String LAB_BILLING_REGISTER_JASPER = "Lab_billing_register";
    public static final String LAB_BILLING_REGISTER_REPORT = "LabBillingRegisterReport";

    public static final String RADIOLOGY_BILLING_REGISTER_JASPER = "Radiology_billing_register";
    public static final String RADIOLOGY_BILLING_REGISTER_REPORT = "RadiologyBillingRegisterReport";

    public static final String DAILY_CASH_COLLECTION_JASPER = "Daily_cash_collection";
    public static final String DAILY_CASH_COLLECTION_REPORT = "DailyCashCollectionReport";

    public static final String CASHIER_WISE_COLLECTION_JASPER = "Cashier_wise_collection";
    public static final String CASHIER_WISE_COLLECTION_REPORT = "CashierWiseCollection";

    public static final String BLOOD_INVENTORY_STOCK_SUMMARY_JASPER = "Blood_inventory_stock_summary";
    public static final String BLOOD_INVENTORY_STOCK_SUMMARY_REPORT = "BloodInventoryStockSummary";

    public static final String BLOOD_INVENTORY_STOCK_DETAIL_JASPER = "Blood_inventory_stock_detail";
    public static final String BLOOD_INVENTORY_STOCK_DETAIL_REPORT = "BloodInventoryStockDetail";

    public static final String COMPONENT_WISE_STOCK_JASPER = "Component_wise_stock_report";
    public static final String COMPONENT_WISE_STOCK_REPORT = "ComponentWiseStockReport";

    public static final String IP_DAILY_CASE_SHEET_JASPER = "Daily_case_sheet";
    public static final String IP_DAILY_CASE_SHEET_REPORT = "dailyCaseSheet";

    public static final String IP_VITALS_JASPER = "Vitals_report";
    public static final String IP_VITALS_REPORT = "vitalsReport";

    public static final String DISCHARGE_SUMMARY_JASPER = "Discharge_summary";
    public static final String DISCHARGE_SUMMARY_REPORT = "dischargeSummary";

    public static final String ADVANCE_RECEIPT_JASPER = "Advance_receipt";
    public static final String ADVANCE_RECEIPT_REPORT = "advanceReceipt";

    public static final String IP_INVESTIGATION_JASPER = "IP_investigation_report";
    public static final String IP_INVESTIGATION_REPORT = "ipInvestigationReport";

    public static final String DRUG_MASTER_JASPER = "Drug_master_report";
    public static final String DRUG_MASTER_REPORT = "drugMasterReport";

    public static final String MEDICAL_CONSUMABLE_NON_CONSUMABLE_JASPER = "Medical_consumable_non_consumable";
    public static final String MEDICAL_CONSUMABLE_NON_CONSUMABLE_REPORT = "MedicalConsumable/Non-Consumable";

    public static final String IP_INITIAL_ASSESSMENT_JASPER = "IPD_initial_assessment";
    public static final String IP_INITIAL_ASSESSMENT_REPORT = "InitialAssessment";

    public static final String PRESCRIPTION_ISSUE_SLIP_JASPER = "Prescription_issue_slip";
    public static final String PRESCRIPTION_ISSUE_SLIP_REPORT = "PrescriptionIssueSlip";

    public static final String PRESCRIPTION_INVOICE_JASPER = "Prescription_invoice";
    public static final String PRESCRIPTION_INVOICE_REPORT = "PrescriptionInvoice";
}
