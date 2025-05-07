package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgOrderDt;
import com.hims.entity.DgOrderHd;
import com.hims.entity.PackageInvestigationMapping;
import com.hims.entity.repository.DgInvestigationPackageRepository;
import com.hims.entity.repository.LabHdRepository;
import com.hims.entity.repository.PackageInvestigationMappingRepository;
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

import java.util.List;

@Service
@Transactional
public class LabRegistrationServicesImpl implements LabRegistrationServices {
    private static final Logger log = LoggerFactory.getLogger(LabRegistrationServicesImpl.class);
    @Autowired
    LabHdRepository labHdRepository;
    @Autowired
    DgMasInvestigation investigation;
    @Autowired
    PackageInvestigationMappingRepository packageInvestigationMappingRepository;
    @Autowired
    DgInvestigationPackageRepository dgInvestigationPackageRepository;
    @Override
    public ApiResponse<AppsetupResponse> labReg(LabRegRequest labReq) {
        {
            AppsetupResponse res = new AppsetupResponse();
            DgOrderHd hd= new DgOrderHd();
            try {
                /// for investigation  data save hd &dt
                if(!labReq.getLabInvestigationReq().isEmpty()) {
                    hd.setAppointmentDate(labReq.getLabInvestigationReq().get(0).getAppointmentDate());
                    hd.setOrderNo("123");//generate no
                    DgOrderHd hdId = labHdRepository.save(hd);
                    //appointment_date
                    DgOrderDt ht = new DgOrderDt();
                    for (LabInvestigationReq key : labReq.getLabInvestigationReq()) {
                        //ht.setInvestigationId(key.getInvestigationId());
                        ht.setOrderhdId(hdId);

                        //call save for dt  here
                    }
                }
                /// for Package data save hd &dt
                if(!labReq.getLabPackegReqs().isEmpty()) {
                    for (LabPackegReq key : labReq.getLabPackegReqs()) {
                        DgOrderHd hd1= new DgOrderHd();
                        hd1.setAppointmentDate(key.getAppointmentDate());
                        hd1.setOrderNo("123");
                        DgOrderHd hdId=labHdRepository.save(hd1);
                        Long PackegId = key.getPackegId();
                        List<PackageInvestigationMapping> investi= packageInvestigationMappingRepository.findByPackage(dgInvestigationPackageRepository.findById(PackegId).get());

                         for( int i=0;i<investi.size();i++){
                             DgOrderDt ht1= new DgOrderDt();
                             DgMasInvestigation dg = investi.get(i).getInvestId();
                             ht1.setOrderhdId(hdId);
                             ht1.setInvestigationId(dg);
                    ///call save for dt  here
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
