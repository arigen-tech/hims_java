package com.hims.notification;

import com.hims.constants.SMSTemplate;
import com.hims.service.WhatsAppNotificationService;
import com.hims.utils.NotificationEvent;
import com.hims.utils.SMSUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final SMSUtility smsUtility;
    private final WhatsAppNotificationService whatsAppNotificationService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleNotification(NotificationEvent event) {

        switch (event.type()) {

            case OTHER_APPOINTMENT_BOOKED ->
                    sendAppointmentBooked(event);

            case APPOINTMENT_CANCELLED ->
                    sendAppointmentCancelled(event);

            case APPOINTMENT_RESCHEDULED ->
                    sendAppointmentRescheduled(event);

            case INVOICE_GENERATED ->
                    sendInvoiceGenerated(event);

            case PATIENT_REGISTERED ->
                    sendPatientRegistered(event);

            case PAYMENT_SUCCESS ->
                    sendPaymentSuccess(event);

            case PAYMENT_FAILED ->
                    sendPaymentFailed(event);

            case REFUND_INITIATED ->
                    sendRefundInitiated(event);

            case REFUND_COMPLETED ->
                    sendRefundCompleted(event);
        }
    }

    private void sendAppointmentBooked(NotificationEvent event) {

        Map<String, String> data = event.variables();

        String patientName = data.get("var1");
        String department = data.get("var2");
        String appointmentDate = data.get("var4");

        // SMS
        try {

            smsUtility.sendSMS(
                    event.mobileNumber(),
                    SMSTemplate.OTHER_APPOINTMENT,
                    data
            );

            log.info(
                    "Appointment booked SMS sent to {}",
                    event.mobileNumber()
            );

        } catch (Exception e) {

            log.error(
                    "Appointment booked SMS failed for {}",
                    event.mobileNumber(),
                    e
            );
        }

        // WhatsApp
        try {

            String message = String.format(
                    "Dear %s, your %s appointment has been successfully booked on %s. Regards, ARIHLT",
                    patientName,
                    department,
                    appointmentDate
            );

            whatsAppNotificationService.sendFreeformText(
                    event.mobileNumber(),
                    message
            );

            log.info(
                    "Appointment booked WhatsApp sent to {}",
                    event.mobileNumber()
            );

        } catch (Exception e) {

            log.error(
                    "Appointment booked WhatsApp failed for {}",
                    event.mobileNumber(),
                    e
            );
        }
    }

    private void sendAppointmentCancelled(NotificationEvent event) {

        Map<String, String> variables = event.variables();

        try {

            smsUtility.sendSMS(
                    event.mobileNumber(),
                    SMSTemplate.APPOINTMENT_CANCEL,
                    variables
            );

        } catch (Exception e) {
            log.error("Appointment cancellation SMS failed", e);
        }

        try {

            String message = String.format(
                    "Dear %s, your %s appointment scheduled on %s has been cancelled. " +
                            "For assistance or to book another appointment, please contact us at %s. " +
                            "Regards, ARIHLT",
                    variables.get("var1"),
                    variables.get("var2"),
                    variables.get("var3"),
                    variables.get("var5")
            );

            whatsAppNotificationService.sendFreeformText(
                    event.mobileNumber(),
                    message
            );

        } catch (Exception e) {
            log.error("Appointment cancellation WhatsApp failed", e);
        }
    }

    private void sendAppointmentRescheduled(NotificationEvent event) {

        Map<String, String> data = event.variables();

        try {

            smsUtility.sendSMS(
                    event.mobileNumber(),
                    SMSTemplate.RESCHEDULED_APPOINTMENT,
                    data
            );

        } catch (Exception e) {

            log.error(
                    "Appointment reschedule SMS failed",
                    e
            );
        }

        try {

            String message = String.format(
                    "Dear %s, your %s appointment has been rescheduled to %s at %s."+
                            " For any queries, please contact us at %s. Regards, ARIHLT",
                    data.get("var1"),
                    data.get("var2"),
                    data.get("var3"),
                    data.get("var5")
            );

            whatsAppNotificationService.sendFreeformText(
                    event.mobileNumber(),
                    message
            );

        } catch (Exception e) {

            log.error(
                    "Appointment reschedule WhatsApp failed",
                    e
            );
        }
    }

    private void sendInvoiceGenerated(NotificationEvent event) {

        Map<String, String> data = event.variables();

        try {

            smsUtility.sendSMS(
                    event.mobileNumber(),
                    SMSTemplate.INVOICE_NOTIFICATION,
                    data
            );


        } catch (Exception e) {

            log.error("Invoice SMS failed", e);
        }

        try {

            String message = String.format(
                    "Dear %s, this is to inform you that an invoice %s of Rs. %s has been generated "+
                     "for your %s service. Payment mode: %s.For any queries, please contact us at %s.Regards,"+
                            " ARIHLT",
                    data.get("var1"),
                    data.get("var2"),
                    data.get("var3"),
                    data.get("var4"),
                    data.get("var5"),
                    data.get("var6")
            );

            whatsAppNotificationService.sendFreeformText(
                    event.mobileNumber(),
                    message
            );

        } catch (Exception e) {

            log.error("Invoice WhatsApp failed", e);
        }
    }

    private void sendPatientRegistered(NotificationEvent event) {

        Map<String, String> data = event.variables();

        String patientName = data.get("patientName");
        String registrationNumber = data.get("registrationNumber");

        try {

            // SMS logic

        } catch (Exception e) {

            log.error("Patient registration SMS failed", e);
        }

        try {

            String message = String.format(
                    "Dear %s, your registration has been successfully completed. Registration No: %s. Regards, ARIHLT",
                    patientName,
                    registrationNumber
            );

            whatsAppNotificationService.sendFreeformText(
                    event.mobileNumber(),
                    message
            );

        } catch (Exception e) {

            log.error("Patient registration WhatsApp failed", e);
        }
    }

    private void sendPaymentSuccess(NotificationEvent event) {
        // payment success notification
    }

    private void sendPaymentFailed(NotificationEvent event) {
        // payment failed notification
    }

    private void sendRefundInitiated(NotificationEvent event) {
        // refund initiated notification
    }

    private void sendRefundCompleted(NotificationEvent event) {
        // refund completed notification
    }
}