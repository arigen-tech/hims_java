package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.LabInvestigationReq;
import com.hims.request.LabPackegReq;
import com.hims.request.LabRegRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.service.LabRegistrationServices;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    MasHospitalRepository masHospitalRepository;
    private final RandomNumGenerator randomNumGenerator;

    public  LabRegistrationServicesImpl(RandomNumGenerator randomNumGenerator) {
        this.randomNumGenerator = randomNumGenerator;
    }

    public String createInvoice() {
        return randomNumGenerator.generateOrderNumber("HIMS",true,true);
    }

    @Override
    @Transactional
    public ApiResponse<AppsetupResponse> labReg(LabRegRequest labReq) {
        {
            AppsetupResponse res = new AppsetupResponse();
            // write logic here for save visit data

            Optional<Patient> patient = patientRepository.findById(labReq.getPatientId());
            Optional<MasHospital> masHospital= masHospitalRepository.findById(12l);
            Visit v=new Visit();
                v.setPatient(patient.get());
                v.setVisitStatus("P");
                v.setVisitStatus("n");
                v.setBillingStatus("g");
                v.setHospital(masHospital.get());
                v.setTokenNo(6l);
                v.setPreConsultation("y");
                //v.setVisitDate();
                //v.setSession();
                //v.setPriority();
                //v.setDepartment();
               Visit  newv = visitRepository.save(v);


            try {
                // for investigation  data save hd &dt
                //grouping same date  for header entry..
                Map<LocalDate, List<LabInvestigationReq>> grouped = labReq.getLabInvestigationReq().stream()
                        .collect(Collectors.groupingBy(LabInvestigationReq::getAppointmentDate));
                           //System.out.println("Duplicate appointmentDate found: " + grouped);
                    grouped.forEach((date, objects) -> {
                    DgOrderHd hd= new DgOrderHd();
                    //System.out.println("Date: " + date);

                   // header entry code  for date
                    hd.setAppointmentDate(date);
                    hd.setOrderDate(LocalDate.now());

                    String orderNum = createInvoice();
                    hd.setOrderNo(orderNum);

                    hd.setOrderStatus("p");
                    hd.setCollectionStatus("p");
                    hd.setPaymentStatus("p");
                    hd.setCreatedBy("23");
                    hd.setHospitalId(12);
                    hd.setDiscountId(1);
                    hd.setPatientId(patient.get());
                    hd.setVisitId(newv);
                    DgOrderHd hdId = labHdRepository.save(hd);

                    for (LabInvestigationReq obj : objects) {
                        DgOrderDt dtInvesti = new DgOrderDt();
                        Optional<DgMasInvestigation> dgMasInvestigation = investigation.findById(obj.getInvestigationId());
                        dtInvesti.setInvestigationId(dgMasInvestigation.get());
                        dtInvesti.setOrderhdId(hdId);
                        dtInvesti.setCreatedBy("23");
                        dtInvesti.setMainChargecodeId(1);
                        // ht.setPackageId(null);
                        dtInvesti.setAppointmentDate(obj.getAppointmentDate());
                        DgOrderDt dtId = labDtRepository.save(dtInvesti);
                    }
                   // System.out.println();
                });

             //   / for Package data save hd &dt
                if(!labReq.getLabPackegReqs().isEmpty()) {
                    for (LabPackegReq key : labReq.getLabPackegReqs()) {
                        DgOrderHd hd1= new DgOrderHd();
                        hd1.setAppointmentDate(key.getAppointmentDate());
                        hd1.setOrderDate(LocalDate.now());

                        String formattedOrderNo =createInvoice();
                        hd1.setOrderNo(formattedOrderNo);

                        hd1.setOrderStatus("p");
                        hd1.setCollectionStatus("p");
                        hd1.setPaymentStatus("p");
                        hd1.setCreatedBy("23");
                        hd1.setHospitalId(12);
                        hd1.setDiscountId(1);
                        hd1.setPatientId(patient.get());
                        hd1.setVisitId(newv);
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
