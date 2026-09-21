package com.hims.constants;
public enum SMSTemplate {

    REGISTRATION("ARIHLT_REG", "Registration"),

    OPD_INVOICE("ARIHLT_OPDINV", "OPD Invoice"),

    LAB_REPORT("ARIHLT_LABREPORT", "Lab Report"),

    APPOINTMENT_CONFIRMATION(
            "ARIHLT_APPTCONFIRM",
            "Appointment Confirmation"
    ),

    LOGIN_OTP("ARIHLT_LOGINOTP", "Login OTP"),

    ADMISSION_CONFIRMATION(
            "Admission Confirmation",
            "Admission Confirmation"
    ),

    APPOINTMENT_CANCEL(
            "AppointmentCancel",
            "Appointment Cancellation"
    ),

    ADVANCE_DEPOSIT(
            "AdvanceDeposit",
            "Advance Deposit"
    ),

    OTHER_APPOINTMENT(
            "OtherAppointment",
            "Appointment"
    ),

    DISCHARGE_NOTIFICATION(
            "DischargeNotification",
            "Discharge Notification"
    ),

    RESCHEDULED_APPOINTMENT(
            "RescheduledAppointment",
            "Rescheduled Appointment"
    );

    private final String templateName;
    private final String description;

    SMSTemplate(String templateName, String description) {
        this.templateName = templateName;
        this.description = description;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDescription() {
        return description;
    }
}