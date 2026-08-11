package com.hims.constants;

public class AppConstants {

    public static final String ENTITY_STORE_INTERNAL_INDENT_M = "StoreInternalIndentM";
    public static final String ENTITY_STORE_INTERNAL_INDENT_T = "StoreInternalIndentT";
    public static final String M_COLUMN_NAME = "status";
    public static final String T_COLUMN_NAME = "issue_status";
    public static final String INDENT_APPROVED_AT_REQ_DEPT = "A";
    public static final String INDENT_REJECTED_AT_REQ_DEPT = "R";
    public static final String INDENT_APPROVED_AT_ISSUED_DEPT = "AA";
    public static final String INDENT_ISSUED_AT_ISSUED_DEPT = "FI";
    public static final String INDENT_REJECTED_AT_ISSUED_DEPT = "RR";
    public static final String INDENT_SUBMITTED_AT_REQ_DEPT = "Y";
    public static final String INDENT_RECEIVED_AT_REQ_DEPT = "RC";
    public static final String INDENT_CREATED_AT_REQ_DEPT = "S";
    public static final String INDENT_NOT_ISSUED_AT_ISSUE_DEPT = "N";
    public static final String ENTITY_STORE_ISSUE_M = "StoreIssueM";
    public static final String INDENT_ISSUED_AT_ISSUE_DEPT = "I";
    public static final String COLUMN_NAME = "status";
    public static final String ENTITY_STORE_ISSUE_T = "StoreIssueT";
    public static final String STATUS_Y = "Y";
    public static final String ENTITY_STORE_INDENT_RECEIVE_M = "StoreIndentReceiveM";
    public static final String STATUS_R = "R";
    public static final String COLUMN_NAME_IS_RETURN = "is_return";
    public static final String STATUS_N = "N";

    //Visit Status
    public static final String VISIT_STATUS_PENDING = "N";
    public static final String VISIT_STATUS_COMPLETED = "Y";
    public static final String VISIT_STATUS_CANCELLED = "C";
    public static final String VISIT_STATUS_CLOSED = "X";
    //billing status
    public static final String PAYMENT_PENDING = "P";
    public static final String PAYMENT_PAID = "Y";
    public static final String PAYMENT_NOT_PAID = "N";

    //blood bank status
    public static final String DONOR_SCREENING_STATUS_PASS = "P";
    public static final String DONOR_SCREENING_STATUS_FAIL = "F";
    public static final String DONOR_SCREENING_TEMPORARILY_DEFERRED	 = "T";
    public static final String DONOR_SCREENING_PERMANENTLY_DEFERRED= "P";
    public static final String COMPONENT_PRBC= "prbc";
    public static final String COMPONENT_PLT= "plt" ;
    public static final String COMPONENT_PLASMA= "plasma" ;
    public static final String COMPONENT_CRYO= "cryo" ;
    public static final String SUMMARY = "S";
    public static final String REACTIVE = "REACTIVE";



    public static final String INTERNAL_SERVER_ERR_MSG="Internal server error !";
    public static final String INDENT_M_NOT_FOUND_MSG="Invalid indentMId , indentMId is not found !";
    public static final String CURRENT_USER_NOT_FOUND_MSG="Current User Not Found";
    public static final String DEPARTMENT_NAME_ADMIN="ADMIN";
    public static final String ITEM_NOT_FOUND_ERR_MSG="Item not found";
    public static final String DEPT_NOT_FOUND_ERR_MSG="Department not found";
    public static final String STATUS_NOT_FOUND_ERR_MSG="Invalid status ! Status not found";
    public static final String CURRENT_DEPT_NOT_FOUND_ERR_MSG="Current department not found";
    public static final String INDENT_HEADER_NOT_FOUND_ERR_MSG="Invalid indent MID ! Indent header not found";
    public static final String INDENT_DETAILS_NOT_FOUND_ERR_MSG="Invalid indent TID ! Indent details not found";
    public static  final String INDENT_NUM_GENERATION_PREFIX="IND-";
    public static final String ISSUE_NUM_GENERATION_PREFIX="ISS-";
    public static final String RETURN_NUM_GENERATION_PREFIX="RET-";
    public static final String BALANCE_NUM_GENERATION_PREFIX="BAL";
    public static final String ITEM_TYPE_DRUG ="D";
    public static final String ITEM_TYPE_NON_DRUG ="N";
    public static final String INDENT_APPROVED_WARNING_MSG="Only pending indents can be approved or rejected.";
    public static final String ACTION_APPROVED="approved";
    public static final String ACTION_REJECTED="rejected";
    public static  final String INVALID_ACTION_WARNING_MSG="Invalid action. Must be approved or rejected";
    public static final String INDENT_ISSUE_HEADER_NOT_FOUND_ERR_MSG="Invalid issue MID ! Issue header not found";
    public static final String INDENT_RECEIVE_HEADER_NOT_FOUND_ERR_MSG="Invalid issue MID ! Issue header not found";
    public static final String ATLEAST_ONE_INDENT_ISSUE_WARN_MSG="At least one item must be issued";
    public static final String ATLEAST_ONE_INDENT_RECEIVE_WARN_MSG="At least one item must be received";
    public  static final String ALREADY_RECEIVED_WARN_MSG="This indent has already been received";
    public static final String ISSUED_MORE_THAN_APPROVED_WARN_MSG="Issuing more than approved quantity";
    public static final String STOCK_NOT_AVAILABLE_WARN_MSG="No stock available for this item ";
    public static  final String ITEM_NOT_ISSUED_MSG="No items were issued";
    public static final String DATE_TIME_FORMAT_FOR_RANDOM_NO_GENERATION="yyyyMMddHHmmss";
    public static final String TRANSACTION_TYPE_AND_SOURCE_ISSUE="ISSUE";
    public static final String TRANSACTION_TYPE_AND_SOURCE_RECEIVE="RECEIVED";
    public static final String TRANSACTION_TYPE_AND_SOURCE_RETURN="RETURNED";
    public  static  final  String SUCCESS_MSG="success";
    public static final String RECEIVING_DEPT_NOT_FOUND_ERR_MSG="Receiving department not found";
    public  static  final  String FALLBACK_ITEM_NAME="Unknown Item";
    public  static  final  String NOT_APPLICABLE="N/A";
    public static final String STRING_FORMATTER_FOR_RECEIVING_VALIDATION="For Item: %s, Batch: %s\n\nReceived (%s) + Rejected (%s) = %s\nBut Issued Quantity is %s\n\nThey must be equal to proceed.";
    public  static final String INDENT_DETAILS_NOT_BELONG_TO_INDENT_M_WARN_MSG="Indent details do not belong to the given indentMId";
    public static final String INDENT_ISSUE_DETAILS_NOT_FOUND_ERR_MSG="Issue details not found";
    public static  final String INDENT_RECEIVE_SUCCESS_MSG="Indent received successfully";
    public  static  final String RETURN_CREATED_FOR_REJECTED_ITEMS_MSG="Return created for rejected items successfully";
    public static final String STOCK_NOT_FOUND_ERR_MSG="Invalid stock Id ,Stock not found ";
    public  static final String BALANCE_SAVED_STATUS="S";
    public static final String BALANCE_SUBMIT_STATUS="P";
    public static final String BALANCE_APPROVE_STATUS="A";
    public static final  String MANUFACTURER_NOT_FOUND_ERR_MSG="Invalid Manufacture Id ,Manufacturer not found";
    public  static final String OPENING_BALANCE_ENTRY_SAVED_SUCCESS_MSG="Opening balance entry saved successfully";
    public static  final  String OPENING_BALANCE_ENTRY_SUBMIT_SUCCESS_MSG="Opening balance entry submitted successfully";
    public static final String OPENING_BALANCE_HEADER_NOT_FOUND_ERR_MSG="Invalid opening balance entry header Id ,Opening balance entry header not found";
    public static final String OPENING_BALANCE_DETAILS_NOT_FOUND_ERR_MSG="Invalid opening balance entry details Id ,Opening balance entry detail not found";
    public static final String PAST_DATE_NOT_ALLOWED="Past dates are not allowed. Please select today or a future date.";
    public static final String TOKEN_ALREADY_BOOKED="This token has just been booked by another user. Please select a different slot.";
    public static final String SESSION_NOT_CONFIGURED= "AppSetup not configured for this session of the day .";
    public static final String POLICY_NOT_FOUND= "Policy not found";
    public static final String SERVICE_TARIFF_NOT_DEFINED= "Service OPD or Tariff is not defined yet";
    public static final String INVALID_SERVICE_CATEGORY = "Service category not found or invalid";
    public static final String BILLING_RECORDS_NOT_FOUND = "No billing records found";
    public static final String BILLING_HEADER_NOT_FOUND = "BillingHeader not found with id: ";
    public static final String PATIENT_UPDATED_BOOKING_SUCCESS = "Patient updated and booking done successfully";
    public static final String APPOINTMENT_CANCELLED = "Appointment cancelled successfully";



    public static final String SAMPLE_COLLECTION_HEADER_NOT_FOUND_ERR_MSG="Invalid sample header Id , Sample header not found";
    public static final String SAMPLE_COLLECTION_DETAIL_NOT_FOUND_ERR_MSG="Invalid sample details Id , Sample details not found";
    public  static final String INVESTIGATION_VALIDATION_SUCCESS_MSG="Investigation validated successfully";
    public static  final String RESULT_ENTRY_HEADER_NOT_FOUND_ERR_MSG="Invalid result entry header Id , Result entry header not found";
    public static  final String LAB_ORDER_DETAIL_NOT_FOUND_ERR_MSG="Invalid order detail Id , order detail not found";
    public static final String RESULT_VALIDATION_SUCCESS_MSG="Result entry validation updated successfully";
    public  static  final String VISIT_NOT_FOUND_ERR_MSG="Invalid visit Id, Visit not found";
    public  static  final String INPATIENT_NOT_FOUND_ERR_MSG="Invalid Inpatient Id, Inpatient not found";
    public  static  final String INPATIENT_AND_VISIT_NOT_FOUND_ERR_MSG="Visit/Inpatient not found";

    public  static  final  String STATUS_S="S";
    public static final String STATUS_P="P";
    public static final String STATUS_F="F";
    public static final String TIME_FORMAT="HH:mm:ss";
    public static final String VISIT_TYPE_FOLLOW_UP= "F";
    public static final String VISIT_TYPE_NEW = "N";
    public static final String INVESTIGATION = "I";
    public static final String DISPLAY_PATIENT_STATUS = "wp";
    public static final String PAYMENT_PARTIAL_PENDING = "P";
    public static final String PACKAGE = "P";
    public static final String BILLING_REFUND_STATUS_COMPLETED_CODE = STATUS_Y;
    public static final String BILLING_REFUND_STATUS_PENDING_CODE = STATUS_N;
    public static final String BILLING_REFUND_STATUS_COMPLETED_LABEL = "COMPLETED";
    public static final String BILLING_REFUND_STATUS_PENDING_LABEL = "PENDING";


    // Billing Template
    public  static  final String PROCEDURE="PROCEDURE";
    public  static  final String SURGERY="SURGERY";

    // Blood Bank Messages
    public static final String DONOR_ALREADY_REGISTERED_MSG = "Donor already registered with same details";
    public static final String DONOR_REGISTRATION_SUCCESS_MSG = "Donor Registration successfully";
    public static final String DONOR_UPDATE_AND_SCREENING_SUCCESS_MSG = "Donor update successfully and add new screening";
    public static final String BLOOD_COLLECTION_SAVE_SUCCESS_MSG = "blood collection save successfully";
    public static final String COMPONENT_FAILURE_REASON_UPDATE_SUCCESS_MSG = "Component failure reason updated successfully";
    public static final String COMPONENT_GENERATION_SAVE_SUCCESS_MSG = "Component generation saved successfully";
    public static final String MANDATORY_TEST_ENTRY_SUCCESS_MSG = "test entry create successfully";

    // Blood Bank Not Found Messages
    public static final String DONOR_NOT_FOUND_ERR_MSG = "Donor not found";
    public static final String DONOR_ID_NOT_FOUND_ERR_MSG = "donorId not found";
    public static final String SCREENING_ID_NOT_FOUND_ERR_MSG = "screeningId not found";
    public static final String DONATION_TYPE_NOT_FOUND_ERR_MSG = "donationTypeId not found";
    public static final String COLLECTION_TYPE_NOT_FOUND_ERR_MSG = "collectionTypeId not found";
    public static final String BAG_TYPE_NOT_FOUND_ERR_MSG = "bagTypeId not found";
    public static final String BLOOD_DONATION_NOT_FOUND_ERR_MSG = "Blood donation record not found with id: ";
    public static final String COMPONENT_NOT_FOUND_ERR_MSG = "Component not found with id: ";
    public static final String COMPONENT_FAILURE_REASON_NOT_FOUND_ERR_MSG = "Component failure reason not found with id: ";

    // Blood Bank Exception Messages
    public static final String DONOR_SAVE_FAILED_ERR_MSG = "Failed to save donor details";
    public static final String FILE_UPLOAD_FAILED_ERR_MSG = "File upload failed";

    // Opd template
    public static final String TEMPLATE_TYPE_INVESTIGATION = "I";
    public static final String TEMPLATE_TYPE_PRESCRIPTION = "P";
    // Mas Bed
    public static final String BED_STATUS_AVAILABLE = "available";
    public static final String  BED_STATUS_CLEANING_BED= "cleaning";
    public static final String BED_STATUS_OCCUPIED_BED= "occupied";
    // opd
    public static final String OPD_PATIENT= "OPD PATIENT";

    //Ipd

    public static final String IPD_ADMISSION_FLAG= "Y";
    public static final String IPD_BED_TRANSFER_REQUEST= "P";
    public static final String IPD_BED_TRANSFER_STATUS_COMPLETE= "C";
    public static final String IPD_BED_TRANSFER_STATUS_REJECT= "R";
    public static final String WORKING_DIAGNOSIS_TYPE= "W";
    public static final String ICD_DIAGNOSIS_TYPE= "I";
    public static final String DIAGNOSIS_STATUS= "A";
    public static final String IO_TYPE_I= "I";

    public static final String IO_TYPE_O= "O";
    public static final String IP_DISCHARGE_SUMMARY_STATUS_DRAFT= "D";
    public static final String IP_DISCHARGE_SUMMARY_STATUS_SUMMIT= "S";
    public static final String IP_RECEIPT_STATUS= "A";







    public static final String OPDTYPE = "OPD";
    public static final String LABTYPE = "LAB";
    public static final String RADIOTYPE = "RAD";

    public static final String PATIENT_NOT_APPLICABLE_FOR_SERVICE_REGISTRATION = "The patient is currently admitted. Service registration is not allowed for this patient.";
    public static final String PATIENT_STATUS_ADMITTED = "ADMITTED";

    public static final String APPOINTMENT_SETUP_SUCCESS_MSG = "Appointment setup created successfully";
    public static final String APPOINTMENT_UPDATE_SUCCESS_MSG = "Appointment setup updated successfully";

    public static final String DOCTOR_ROSTER_NOT_FOUND_MSG = "Doctor roster is not created yet. Please create a weekly roster Mon-Sat before taking appointments.";
    public static final String CONSULTATION_FEE_NOT_SET_MSG = "Consultation fee not set for this doctor in selected department. Validate tariff before confirming appointment.";
    public static final String SOURCE_TYPE_OPD = "OPD";
    public static final String SOURCE_TYPE_IPD = "IPD";

    public static final String SOURCE_LAB = "lab source";
    public static final String INPATIENT_ISSUE = "IPD_ISSUE";

    public static final String MANDATORY_DIAGNOSIS_MESSAGE = "One is mandatory: Working Diagnosis or ICD Diagnosis";





    public static final String ORDER_HD_NOT_FOUND_MSG = "Invalid order Id , Order Hd Id not found";

    public static final String DUPLICATE_APPOINTMENT_MSG = "Patient already has an appointment with the same doctor on this day.";
    public static final String MSG_INVALID_FLAG = "Invalid flag";


    private AppConstants() {
    }

}