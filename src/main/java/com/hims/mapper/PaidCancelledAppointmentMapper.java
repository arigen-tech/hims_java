//package com.hims.mapper;
//
//import com.hims.projection.PaidCancelledAppointmentProjection;
//import com.hims.request.PaidCancelledAppointmentResponse;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PaidCancelledAppointmentMapper {
//
//    public PaidCancelledAppointmentResponse mapToResponse(
//            PaidCancelledAppointmentProjection projection
//    ) {
//
//        if (projection == null) {
//            return null;
//        }
//
//        PaidCancelledAppointmentResponse response =
//                new PaidCancelledAppointmentResponse();
//
//        response.setVisitId(projection.getVisitId());
//        response.setPatientId(projection.getPatientId());
//        response.setBillingHeaderId(projection.getBillingHeaderId());
//        response.setRegistrationNo(projection.getRegistrationNo());
//        response.setPatientName(projection.getPatientName());
//        response.setMobileNo(projection.getMobileNo());
//        response.setAge(projection.getAge());
//        response.setGender(projection.getGender());
//        response.setBillingType(projection.getBillingType());
//        response.setDate(projection.getDate());
//        response.setBillingAmount(projection.getBillingAmount());
//        response.setCancelledDate(projection.getCancelledDate());
//        response.setPaymentId(projection.getPaymentId());
//        response.setRefundId(projection.getRefundId());
//        response.setRefundDate(projection.getRefundDate());
//        response.setRefundStatus(projection.getRefundStatus());
//        response.setDepartmentName(projection.getDepartmentName());
//
//        return response;
//    }
//}
package com.hims.mapper;

import com.hims.helperUtil.HelperUtils;
import com.hims.projection.PaidCancelledAppointmentProjection;
import com.hims.request.PaidCancelledAppointmentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaidCancelledAppointmentMapper {

    public PaidCancelledAppointmentResponse mapToResponse(
            PaidCancelledAppointmentProjection projection
    ) {

        if (projection == null) {
            return null;
        }

        PaidCancelledAppointmentResponse response =
                new PaidCancelledAppointmentResponse();

        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setBillingHeaderId(projection.getBillingHeaderId());

        response.setPatientName(projection.getPatientName());
        response.setMobileNo(projection.getMobileNo());
        response.setAge(projection.getAge());
        response.setGender(projection.getGender());

        response.setBillingType(projection.getBillingType());

        response.setDate(projection.getDate());
        response.setBillDate(HelperUtils.instantToLocalDateTime(projection.getBillDate()));
        response.setCancelledDate(projection.getCancelledDate());

        response.setBillingAmount(projection.getBillingAmount());

        response.setPaymentId(projection.getPaymentId());

        response.setRefundId(projection.getRefundId());
        response.setRefundAmount(projection.getRefundAmount());
        response.setRefundReferenceNo(projection.getRefundReferenceNo());
        response.setRefundReason(projection.getRefundReason());
        response.setGatewayRefundId(projection.getGatewayRefundId());
        response.setRefundDate(projection.getRefundDate());
        response.setRefundStatus(projection.getRefundStatus());

        response.setPaymentModeId(projection.getPaymentModeId());
        response.setPaymentModeCode(projection.getPaymentModeCode());
        response.setPaymentModeName(projection.getPaymentModeName());

        response.setDepartmentName(projection.getDepartmentName());

        return response;
    }
}
