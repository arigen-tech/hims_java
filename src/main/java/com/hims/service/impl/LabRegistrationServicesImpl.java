package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgOrderDt;
import com.hims.entity.DgOrderHd;
import com.hims.entity.PackageInvestigationMapping;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.LabInvestigationReq;
import com.hims.request.LabPackegReq;
import com.hims.request.LabRegRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.service.LabRegistrationServices;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service

public class LabRegistrationServicesImpl implements LabRegistrationServices {
    private static final Logger log = LoggerFactory.getLogger(LabRegistrationServicesImpl.class);
    @Autowired
    LabHdRepository labHdRepository;
    @Autowired
    DgMasInvestigationRepository investigation;
    @Autowired
    LabDtRepository labDtRepository;
    @Autowired
    PackageInvestigationMappingRepository packageInvestigationMappingRepository;
    @Autowired
    DgInvestigationPackageRepository dgInvestigationPackageRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    VisitRepository visitRepository;
    @Override
    @Transactional
    public ApiResponse<AppsetupResponse> labReg(LabRegRequest labReq) {
        {
            AppsetupResponse res = new AppsetupResponse();
            // write logic here for save visit data



            DgOrderHd hd= new DgOrderHd();
            try {
                /// for investigation  data save hd &dt
                if(!labReq.getLabInvestigationReq().isEmpty()) {
                    hd.setAppointmentDate(labReq.getLabInvestigationReq().get(0).getAppointmentDate());
                    hd.setOrderDate(LocalDate.now());
                    hd.setOrderNo("123");//run time grnerate
                    hd.setOrderStatus("p");
                    hd.setCollectionStatus("p");
                    hd.setPaymentStatus("p");
                    hd.setCreatedBy("23");
                    hd.setHospitalId(12);
                    hd.setDiscountId(1);
                    hd.setPatientId(patientRepository.findById((long)27).get());
                    hd.setVisitId(visitRepository.findById((long)34).get());
                    DgOrderHd hdId = labHdRepository.save(hd);

                    for (LabInvestigationReq key : labReq.getLabInvestigationReq()) {
                        DgOrderDt htInvesti = new DgOrderDt();
                        Optional<DgMasInvestigation> dgMasInvestigation=investigation.findById(key.getInvestigationId());
                        htInvesti.setInvestigationId(dgMasInvestigation.get());
                        htInvesti.setOrderhdId(hdId);
                        htInvesti.setCreatedBy("23");
                        htInvesti.setMainChargecodeId(1);
                       // ht.setPackageId(null);
                        htInvesti.setAppointmentDate(labReq.getLabInvestigationReq().get(0).getAppointmentDate());

                        DgOrderDt dtId = labDtRepository.save(htInvesti);
                        //call save for dt  here
                    }
                }
                /// for Package data save hd &dt
                if(!labReq.getLabPackegReqs().isEmpty()) {
                    for (LabPackegReq key : labReq.getLabPackegReqs()) {
                        DgOrderHd hd1= new DgOrderHd();
                        hd1.setAppointmentDate(key.getAppointmentDate());
                        hd1.setOrderDate(LocalDate.now());
                        hd1.setOrderNo("123");//run time grnerate
                        hd1.setOrderStatus("p");
                        hd1.setCollectionStatus("p");
                        hd1.setPaymentStatus("p");
                        hd1.setCreatedBy("23");
                        hd1.setHospitalId(12);
                        hd1.setDiscountId(1);
                        hd1.setPatientId(patientRepository.findById((long)27).get());
                        hd1.setVisitId(visitRepository.findById((long)34).get());
                        DgOrderHd hdId1=labHdRepository.save(hd1);

                        Long PackegId = key.getPackegId();
                        List<PackageInvestigationMapping> investi= packageInvestigationMappingRepository.findByPackageId(dgInvestigationPackageRepository.findById(PackegId).get());

                         for( int i=0;i<investi.size();i++){
                             DgOrderDt htPkg= new DgOrderDt();
                             DgMasInvestigation dg = investi.get(i).getInvestId();
                             htPkg.setOrderhdId(hdId1);
                             htPkg.setInvestigationId(dg);
                             htPkg.setCreatedBy("23");
                             htPkg.setMainChargecodeId(1);
                             htPkg.setPackageId(dgInvestigationPackageRepository.findById(PackegId).get());
                             htPkg.setAppointmentDate(key.getAppointmentDate());
                             DgOrderDt dtId = labDtRepository.save(htPkg);
                         }
                    }
                }
                res.setMsg("Success");
                return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
            } catch (SDDException e) {
                return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {}, e.getMessage(), e.getStatus());
            } catch (Exception e) {
                return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {}, "Internal Server Error", 500);
            }
        }
    }
}
