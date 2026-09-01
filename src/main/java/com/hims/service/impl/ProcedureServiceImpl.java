package com.hims.service.impl;

import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.projection.ProcedureWorklistProjection;
import com.hims.request.*;
import com.hims.response.ProcedureDetailResponse;
import com.hims.response.ProcedureResponse;
import com.hims.response.ProcedureWorklistResponse;
import com.hims.service.ProcedureService;
import com.hims.service.TransactionSequenceService;
import com.hims.utils.AuthUtil;
import com.hims.utils.HMISTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcedureServiceImpl implements ProcedureService {

    private final ProcedureHdRepository procedureHdRepository;
    private final ProcedureDtRepository procedureDtRepository;
    private final ProcedureSessionRepository procedureSessionRepository;
    private final MasProcedureTypeRepository  masProcedureTypeRepository;
    private final MasProcedureStatusRepository  masProcedureStatusRepository;
    private final MasProcedureRepository masProcedureRepository;
    private final TransactionSequenceService transactionSequenceService;
    @Value("${procedure.status.pending.id}")
    private Long procedurePendingStatusId;
    private final AuthUtil authUtil;
    private final DentalProcedureToothRepository dentalProcedureToothRepository;
    private final MasToothMasterRepository  masToothMasterRepository;


    @Override
    @Transactional
    public ProcedureResponse createProcedure(ProcedureRequestHd request,String billingMethod,String priority) {

        ProcedureHd procedureHd = createProcedureHeader(request, priority);

        List<ProcedureDetailResponse> details = new ArrayList<>();

        for (int i = 0; i < request.getProcedureDetails().size(); i++) {
            ProcedureItemRequest item = request.getProcedureDetails().get(i);

            ProcedureDt procedureDt = createProcedureDetail(procedureHd,item,i + 1,billingMethod);

            List<Long> sessionIds = createSessions(procedureDt, item);

            // Save dental procedure teeth only if provided
            createDentalProcedureTeeth(procedureDt, item);

            details.add(ProcedureDetailResponse.builder()
                    .procedureDtId( procedureDt.getProcedureDtId())
                            .procedureId(procedureDt.getProcedure()
                                    .getProcedureId()).sessionIds(sessionIds).build());
        }

        return ProcedureResponse.builder()
                .procedureHdId(procedureHd.getProcedureHdId())
                .procedureNo(procedureHd.getProcedureNo())
                .procedures(details)
                .build();
    }

    private ProcedureHd createProcedureHeader(ProcedureRequestHd request,String priority) {
        User user = authUtil.getCurrentUser();
        if (user == null) {
            throw new SDDException("user",
                    401,
                    "Authenticated user not found"
            );
        }
        ProcedureHd procedureHd = new ProcedureHd();
        procedureHd.setPatientId(request.getPatientId());
        procedureHd.setProcedureNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.PROCEDURE_NO, user.getHospital().getId()));
        procedureHd.setVisitId(request.getVisitId());
        procedureHd.setDepartmentId(request.getDepartmentId());
        MasProcedureType procedureType =
                masProcedureTypeRepository.findByProcedureTypeCode(request.getProcedureTypeCode()).orElseThrow(() ->
                        new SDDException( "procedureType", 404, "Procedure type not found with id: " + request.getProcedureTypeCode()));
        procedureHd.setProcedureType(procedureType);
        procedureHd.setAdvisedBy(authUtil.getCurrentUserFullName());
        procedureHd.setAdvisedDate(LocalDateTime.now());
        procedureHd.setDiagnosis(request.getDiagnosis());
        procedureHd.setPriority(priority);
        procedureHd.setProcedureStatus(
                masProcedureStatusRepository.findById(procedurePendingStatusId).orElseThrow(() ->
                        new SDDException("procedureStatus", 404, "Pending procedure status not found"))
        );
        procedureHd.setPaymentStatus(AppConstants.PAYMENT_NOT_PAID);
        procedureHd.setStatus(AppConstants.STATUS_N);
        procedureHd.setCreatedBy(authUtil.getCurrentUserFullName());
        procedureHd.setCreatedDate(LocalDateTime.now());
        procedureHd.setLastUpdatedBy(authUtil.getCurrentUserFullName());
        procedureHd.setLastUpdateDate(LocalDateTime.now());
        return procedureHdRepository.save(procedureHd);
    }
    private ProcedureDt createProcedureDetail(ProcedureHd procedureHd,ProcedureItemRequest request,int sequenceNo,String billingMethod) {
        if (request.getProcedureId() == null) {
            throw new SDDException("procedureId", 400, "Procedure ID cannot be null");
        }

        MasProcedure procedure = masProcedureRepository.findById(request.getProcedureId()
                ).orElseThrow(() ->
                        new SDDException("procedure", 404, "Procedure not found with id: " + request.getProcedureId()));
        ProcedureDt procedureDt = new ProcedureDt();
        procedureDt.setProcedureHd(procedureHd);
        procedureDt.setProcedure(procedure);
        procedureDt.setSequenceNo(sequenceNo);
        int plannedSessions = request.getPlannedSessionCount() != null ? request.getPlannedSessionCount() : 1;
        procedureDt.setPlannedSessionCount(plannedSessions);
        procedureDt.setCompletedSessionCount(0);
        procedureDt.setBillingStatus(AppConstants.BILLING_PENDING_LABEL);
        procedureDt.setBillingMethod(billingMethod);
        procedureDt.setProcedureStatus(
                masProcedureStatusRepository.findById(
                        procedurePendingStatusId
                ).orElseThrow(() ->
                        new SDDException(
                                "procedureStatus",
                                404,
                                "Pending procedure status not found"
                        )
                )
        );

        procedureDt.setRemarks(request.getRemarks());
        procedureDt.setStatus(AppConstants.STATUS_N);
        procedureDt.setCreatedBy(authUtil.getCurrentUserFullName());
        procedureDt.setCreatedDate(LocalDateTime.now());
        procedureDt.setLastUpdatedBy(authUtil.getCurrentUserFullName());
        procedureDt.setLastUpdateDate(LocalDateTime.now());
        return procedureDtRepository.save(procedureDt);
    }
    private List<Long> createSessions(ProcedureDt procedureDt, ProcedureItemRequest request) {

        List<Long> sessionIds = new ArrayList<>();
        int plannedSessions = procedureDt.getPlannedSessionCount();

        List<ProcedureSessionRequest> sessionRequests = request.getSessions();
        for (int i = 1; i <= plannedSessions; i++) {
            ProcedureSessionRequest sessionRequest = null;
            if (sessionRequests != null && sessionRequests.size() >= i) {
                sessionRequest = sessionRequests.get(i - 1);
            }

            ProcedureSession session = new ProcedureSession();
            session.setProcedureDt(procedureDt);
            session.setSessionNo(i);
            if (sessionRequest != null) {
                session.setScheduledDateTime(sessionRequest.getScheduledDateTime());
                session.setRemarks(sessionRequest.getRemarks());
            }
            session.setSessionStatus("SCHEDULED");
            session.setIsFinalSession( i == plannedSessions
                            ? AppConstants.STATUS_Y
                            : AppConstants.STATUS_N
            );

            session.setBillingRequired(AppConstants.STATUS_Y);
            session.setBillingStatus(AppConstants.NOT_APPLICABLE_LABEL);
            session.setStatus(AppConstants.STATUS_N);
            session.setCreatedBy(authUtil.getCurrentUserFullName());
            session.setCreatedDate(LocalDateTime.now());
            session.setLastUpdatedBy(authUtil.getCurrentUserFullName() );
            session.setLastUpdateDate(LocalDateTime.now());
            session.setComplicationFlag(AppConstants.STATUS_N);
            session = procedureSessionRepository.save(session);
            sessionIds.add(session.getProcedureSessionId());
        }

        return sessionIds;
    }


    private void createDentalProcedureTeeth(ProcedureDt procedureDt, ProcedureItemRequest request) {
        if (request.getDentalProcedureTeeth() == null || request.getDentalProcedureTeeth().isEmpty()) {

            return;
        }
        for (DentalToothRequest dentalToothRequest : request.getDentalProcedureTeeth()) {
                if (dentalToothRequest.getToothId() == null) { throw new SDDException(
                            "toothId",
                            400,
                            "Tooth ID cannot be null"
                    );
                }
                DentalProcedureTooth dentalProcedureTooth = new DentalProcedureTooth();
                dentalProcedureTooth.setProcedureDt(procedureDt);
                dentalProcedureTooth.setTooth(masToothMasterRepository.getReferenceById(dentalToothRequest.getToothId()));
                dentalProcedureTooth.setToothSurface(dentalToothRequest.getToothSurface());
                dentalProcedureTooth.setStatus(AppConstants.STATUS_N);
                dentalProcedureTooth.setCreatedBy(authUtil.getCurrentUserFullName());
                dentalProcedureTooth.setCreatedDate(LocalDateTime.now());
                dentalProcedureTooth.setLastUpdatedBy(authUtil.getCurrentUserFullName());
                dentalProcedureTooth.setLastUpdateDate(LocalDateTime.now());
                dentalProcedureToothRepository.save(dentalProcedureTooth);
            }
    }


    @Override
    public Page<ProcedureWorklistResponse> getProcedureWorklist(
            String mobileNo,
            String patientName,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProcedureWorklistProjection> result =
                procedureDtRepository.getProcedureWorklist(
                        mobileNo,
                        patientName,
                        AppConstants.STATUS_N,
                        AppConstants.STATUS_N,
                        AppConstants.STATUS_N,
                        pageable
                );

        return result.map(item ->
                ProcedureWorklistResponse.builder()
                        .procedureHdId(item.getProcedureHdId())
                        .procedureDtId(item.getProcedureDtId())
                        .patientId(item.getPatientId())
                        .mobileNo(item.getMobileNo())
                        .patientName(item.getPatientName())
                        .age(item.getAge())
                        .gender(item.getGender())
                        .department(item.getDepartment())
                        .procedure(item.getProcedure())
                        .completedSessions(item.getCompletedSessions())
                        .totalSessions(item.getTotalSessions())
                        .scheduledDateTime(item.getScheduledDateTime())
                        .advisedBy(item.getAdvisedBy())
                        .billingStatus(item.getBillingStatus())
                        .build()
        );
    }
}
