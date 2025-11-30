package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.OpdTemplateService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpdTemplateServiceImpl implements OpdTemplateService {
    private static final Logger log = LoggerFactory.getLogger(OpdTemplateServiceImpl.class);

    @Autowired
    private OpdTemplateRepository opdTempRepo;

    @Autowired
    private OpdTemplateInvestigationRepository opdTempInvestRepo;

    @Autowired
    private  StoreItemBatchStockRepository storeItemBatchStockRepository;

    @Autowired
    private MasStoreItemRepository masStoreItemRepository;

    @Autowired
    private MasDepartmentRepository departmentRepo;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepo;

    @Autowired
    private UserRepo userRepo;


    @Autowired
    private LabHdRepository labHdRepository;

    @Autowired
    private LabDtRepository labDtRepository;

    @Autowired
    private MasFrequencyRepository frequencyRepo;

    @Autowired
    private MasStoreItemRepository itemRepo;

    @Autowired
    private final RandomNumGenerator randomNumGenerator;

    @Autowired
    private OpdTemplateTreatmentRepository opdTemplateTreatmentRepository;

    @Value("${hos.define.dayOfExparyDrug}")
    private Integer hospDefinedDays;

    public OpdTemplateServiceImpl(RandomNumGenerator randomNumGenerator) {
        this.randomNumGenerator = randomNumGenerator;
    }

    public String createInvoice() {
        return randomNumGenerator.generateOrderNumber("HIMS",true,true);
    }

    @Override
    public ApiResponse<OpdTemplateResponse> getByTemplateId(Long templateId) {
        Optional<OpdTemplate> opdtObj = opdTempRepo.findById(templateId);
        if(opdtObj.isPresent()){
            OpdTemplate opdTemp = opdtObj.get();
            List<OpdTemplateInvestigation> opdTempInvestObj = opdTempInvestRepo.findByOpdTemplateId(opdTemp);

            OpdTemplateResponse opdTemplateResp = mapToResponse(opdTemp,opdTempInvestObj);

            return ResponseUtils.createSuccessResponse(opdTemplateResp, new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<OpdTemplateResponse>() {}, "templateId not found", 404);
        }
    }

    @Override
    public ApiResponse<List<OpdTemplateResponse>> getAllTemplateInvestigations(int flag) {
        try {
            List<OpdTemplate> templates;

            if (flag == 1) {
                templates = opdTempRepo.findByStatusIgnoreCase("Y");
            } else if (flag == 0) {
                templates = opdTempRepo.findByStatusInIgnoreCase(List.of("Y", "N"));
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value. Use 0 or 1.", 400);
            }

            templates = templates.stream()
                    .filter(t -> t.getOpdTemplateType() != null && t.getOpdTemplateType().equalsIgnoreCase("I"))
                    .collect(Collectors.toList());

            List<OpdTemplateResponse> responses = templates.stream().map(template -> {
                List<OpdTemplateTreatment> treatments = opdTemplateTreatmentRepository.findByTemplate(template);
                List<OpdTemplateInvestigation> investigations = opdTempInvestRepo.findByOpdTemplateId(template);
                return mapToResponse(template, investigations);
            }).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Failed to fetch OPD Template Treatments: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<OpdTemplateResponse> createOpdTemplate(OpdTemplateRequest opdTempReq) {
        try{
            Long depId = authUtil.getCurrentDepartmentId();
            OpdTemplate opdt = new OpdTemplate();
            opdt.setOpdTemplateName(opdTempReq.getOpdTemplateName());
            opdt.setOpdTemplateCode(opdTempReq.getOpdTemplateCode());
            opdt.setOpdTemplateType("I");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            opdt.setLastChgBy(currentUser.getUsername());
            opdt.setLastChgDate(Instant.now());
            opdt.setStatus("y");
            MasDepartment department = departmentRepo.findById(depId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            opdt.setDepartmentId(department);
            User doctor = userRepo.findById(currentUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            opdt.setDoctorId(doctor);
            OpdTemplate savedOpdTemplate = opdTempRepo.save(opdt);

            List<OpdTemplateInvestigationRequest> templateInvestigationRequests = opdTempReq.getInvestigationRequestList();
            List<OpdTemplateInvestigation> templateInvestigations = new ArrayList<>();
            for (OpdTemplateInvestigationRequest otir : templateInvestigationRequests) {
                OpdTemplateInvestigation tempInvest = new OpdTemplateInvestigation();
                tempInvest.setOpdTemplateId(savedOpdTemplate);
                DgMasInvestigation investigation = dgMasInvestigationRepo.findById(otir.getInvestigationId())
                        .orElseThrow(() -> new RuntimeException("Investigation not found"));
                tempInvest.setInvestigationId(investigation);
                templateInvestigations.add(tempInvest);
            }
            opdTempInvestRepo.saveAll(templateInvestigations);

            OpdTemplateResponse response = mapToResponse(savedOpdTemplate, templateInvestigations);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "The data cannot be created" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Transactional
    public ApiResponse<String> updateOpdTemplate(OpdTempInvReq opdTempInvReq) {
        try{
            Optional<OpdTemplate> opdObj = opdTempRepo.findById(opdTempInvReq.getTemplateId());
            if (opdObj.get() != null){
                for (OpdTemplateInvestigationRequest opdDetail: opdTempInvReq.getOpdTempInvest()){
                    if (opdDetail.getTemplateInvestigationId() == null){
                        OpdTemplateInvestigation newObj = new OpdTemplateInvestigation();
                        Optional<DgMasInvestigation> dg = dgMasInvestigationRepo.findById(opdDetail.getInvestigationId());
                        newObj.setInvestigationId(dg.get());
                        Optional<OpdTemplate> opd1obj = opdTempRepo.findById(opdTempInvReq.getTemplateId());
                        newObj.setOpdTemplateId(opd1obj.get());
                        opdTempInvestRepo.save(newObj);
                    }else {
                        OpdTemplateInvestigation opdDtObj = opdTempInvestRepo.getById(opdDetail.getTemplateInvestigationId());
                        Optional<DgMasInvestigation> dg = dgMasInvestigationRepo.findById(opdDetail.getInvestigationId());
                        opdDtObj.setInvestigationId(dg.get());
                        opdTempInvestRepo.save(opdDtObj);
                    }
                }
                for (Long dtDetailObj : opdTempInvReq.getDeletedTempIvs()){
                    opdTempInvestRepo.deleteById(dtDetailObj);
                }

            }else {
                return ResponseUtils.createSuccessResponse("OpdTemplate is not foud", new TypeReference<>() {
                });
            }

            return ResponseUtils.createSuccessResponse("update Successfully", new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "The data cannot be created" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
//        return null;
    }

    @Override
    public ApiResponse<InvestigationByTemplateResponse> multiInvestigationTemplate(InvestigationByTemplateRequest investByTempReq) {

        Long departmentId = authUtil.getCurrentDepartmentId();
        User currentUser = authUtil.getCurrentUser();

        Optional<OpdTemplate> opdTemplateExisting = opdTempRepo.findById(investByTempReq.getTemplateId());
        if(opdTemplateExisting.isPresent()){
            OpdTemplate opdtObj = opdTemplateExisting.get();

            Map<LocalDate, List<InvestigationForTemplateRequest>> grouped = investByTempReq.getInvestigationForTemplateRequestList().stream()
                    .filter(request -> request.getDateOfOrder() != null)
                    .collect(Collectors.groupingBy(InvestigationForTemplateRequest::getDateOfOrder));

            Map<LocalDate, DgOrderHd> savedHeadersByDate = new HashMap<>();
            Map<Long, DgOrderDt> savedDetailsByInvestigationId = new HashMap<>();
            Map<Long, DgMasInvestigation> investigationEntities = new HashMap<>();
            Map<Long, LocalDate> investigationDates = new HashMap<>();

            for (Map.Entry<LocalDate, List<InvestigationForTemplateRequest>> entry : grouped.entrySet()) {
                LocalDate date = entry.getKey();
                List<InvestigationForTemplateRequest> iftrObj = entry.getValue();

                DgOrderHd hd = new DgOrderHd();
                hd.setAppointmentDate(date);
                hd.setOrderDate(LocalDate.now());
                hd.setOrderNo(createInvoice());
                hd.setOrderStatus("n");
                hd.setCollectionStatus("n");
                hd.setPaymentStatus("n");
                hd.setHospitalId(Math.toIntExact(currentUser.getHospital().getId()));
                hd.setDepartmentId(departmentId.intValue());
                hd.setLastChgBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                hd.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                hd.setCreatedOn(LocalDate.now());
                hd.setLastChgDate(LocalDate.now());
                hd.setLastChgTime(LocalTime.now().toString());
                DgOrderHd savedHd = labHdRepository.save(hd);

                savedHeadersByDate.put(date, savedHd);

                BillingHeader headerId=new BillingHeader();

                for(InvestigationForTemplateRequest investTempReq : iftrObj){
                    Long investId = investTempReq.getInvestigationId();
                    if(investId == null){
                        throw new IllegalArgumentException("investigationId cannot be null");
                    }

                    DgMasInvestigation invEntity = dgMasInvestigationRepo.findById(investId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid Investigation ID: " + investId));

                    DgOrderDt dt = new DgOrderDt();
                    dt.setInvestigationId(invEntity);
                    dt.setOrderhdId(savedHd);
                    dt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
                    dt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());
                    dt.setAppointmentDate(investTempReq.getDateOfOrder());
                    dt.setLastChgBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                    dt.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                    dt.setLastChgDate(LocalDate.now());
                    dt.setBillingStatus("n");
                    dt.setOrderStatus("n");
                    dt.setOrderQty(1);
                    dt.setCreatedon(Instant.now());
                    dt.setLastChgTime(LocalTime.now().toString());

                    DgOrderDt savedDt = labDtRepository.save(dt);
                    savedDt.setBillingHd(headerId);
                    labDtRepository.save(savedDt);

                    savedDetailsByInvestigationId.put(investId, savedDt);
                }
            }
            InvestigationByTemplateResponse response = mapToInvestByTemplateResp(
                    opdtObj,
                    savedHeadersByDate,
                    savedDetailsByInvestigationId,
                    investigationEntities,
                    investigationDates,
                    investByTempReq.getInvestigationIdsToDelete()
            );
            return ResponseUtils.createSuccessResponse(response, new TypeReference<InvestigationByTemplateResponse>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("templateId not found", 404);
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private List<OpdTemplateInvestigationResponse> responseForInvest(
            List<OpdTemplateInvestigation> opdTemplateInvestigationList) {
        return opdTemplateInvestigationList.stream().map(tempInvest -> {
            OpdTemplateInvestigationResponse otir = new OpdTemplateInvestigationResponse();
            otir.setTemplateInvestigationId(tempInvest.getTemplateInvestigationId());
            otir.setOpdTemplateId(
                    tempInvest.getOpdTemplateId() != null ? tempInvest.getOpdTemplateId().getTemplateId() : null
            );
            otir.setInvestigationId(
                    tempInvest.getInvestigationId() != null ? tempInvest.getInvestigationId().getInvestigationId() : null
            );
            return otir;
        }).collect(Collectors.toList());
    }

    private OpdTemplateResponse mapToResponse(
            OpdTemplate opdTemp,
            List<OpdTemplateInvestigation> opdTempInvestList) {
        OpdTemplateResponse opr = new OpdTemplateResponse();
        opr.setTemplateId(opdTemp.getTemplateId());
        opr.setOpdTemplateName(opdTemp.getOpdTemplateName());
        opr.setOpdTemplateCode(opdTemp.getOpdTemplateCode());
        opr.setOpdTemplateType(opdTemp.getOpdTemplateType());
        opr.setLastChgBy(opdTemp.getLastChgBy());
        opr.setLastChgDate(opdTemp.getLastChgDate());
        opr.setStatus(opdTemp.getStatus());
        opr.setDepartmentId(opdTemp.getDepartmentId() != null ? opdTemp.getDepartmentId().getId() : null);
        opr.setDoctorId(opdTemp.getDoctorId() != null ? opdTemp.getDoctorId().getUserId() : null);

        List<OpdTemplateInvestigationResponse> opdTempInvestResponses = opdTempInvestList.stream().map(template ->{
            OpdTemplateInvestigationResponse opir =new OpdTemplateInvestigationResponse();
            opir.setTemplateInvestigationId(template.getTemplateInvestigationId());
            opir.setOpdTemplateId(template.getOpdTemplateId() != null ? template.getOpdTemplateId().getTemplateId() : null);
            opir.setInvestigationId(template.getInvestigationId() != null ? template.getInvestigationId().getInvestigationId() : null);
            opir.setInvestigationName(template.getInvestigationId() != null ? template.getInvestigationId().getInvestigationName() : null);
            return opir;
        }).collect(Collectors.toList());
        opr.setInvestigationResponseList(opdTempInvestResponses);

        return opr;
    }

    private InvestigationByTemplateResponse mapToInvestByTemplateResp(
            OpdTemplate template,
            Map<LocalDate, DgOrderHd> savedHeadersByDate,
            Map<Long, DgOrderDt> savedDetailsByInvestigationId,
            Map<Long, DgMasInvestigation> investigationEntities,
            Map<Long, LocalDate> investigationDates,
            List<Long> investigationIdsToDelete
    ) {
        InvestigationByTemplateResponse response = new InvestigationByTemplateResponse();
        response.setTemplateId(template.getTemplateId());
        response.setTemplateName(template.getOpdTemplateName());

        List<InvestigationForTemplateResponse> responseList = new ArrayList<>();

        for (Map.Entry<Long, DgOrderDt> entry : savedDetailsByInvestigationId.entrySet()) {
            Long investigationId = entry.getKey();
            DgOrderDt dt = entry.getValue();
            DgMasInvestigation invEntity = investigationEntities.get(investigationId);
            LocalDate dateOfOrder = investigationDates.get(investigationId);
            DgOrderHd hd = savedHeadersByDate.get(dateOfOrder);

            if (dt != null && invEntity != null && hd != null) {
                InvestigationForTemplateResponse invResp = new InvestigationForTemplateResponse();
                invResp.setDgOrderHdId(hd.getId());
                invResp.setInvestigationId(invEntity.getInvestigationId());
                invResp.setInvestigationName(invEntity.getInvestigationName());
                invResp.setDateOfOrder(dateOfOrder);
                invResp.setDgOrderDtId(dt.getId());

                responseList.add(invResp);
            }
        }

        response.setInvestigationForTemplateResponseList(responseList);
        response.setInvestigationIdsToDelete(investigationIdsToDelete);

        return response;
    }




    // ======================= OPD TEMPLATE TREATMENT SECTION ======================= //

    @Override
    @Transactional
    public ApiResponse<OpdTemplateResponse> saveOpdTemplateTreatment(OpdTemplateRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            Long depId = authUtil.getCurrentDepartmentId();

            // Create template
            OpdTemplate template = new OpdTemplate();
            template.setOpdTemplateCode(request.getOpdTemplateCode());
            template.setOpdTemplateName(request.getOpdTemplateName());
            template.setOpdTemplateType("P");
            template.setLastChgBy(currentUser.getUsername());
            template.setLastChgDate(Instant.now());
            template.setStatus("y");

            // Department & Doctor mapping
            MasDepartment department = departmentRepo.findById(depId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            template.setDepartmentId(department);

            User doctor = userRepo.findById(currentUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            template.setDoctorId(doctor);

            // Save template first
            OpdTemplate savedTemplate = opdTempRepo.save(template);

            // Map Treatments
            List<OpdTemplateTreatment> treatments = request.getTreatments().stream().map(t -> {
                OpdTemplateTreatment entity = new OpdTemplateTreatment();
                entity.setDosage(t.getDosage());
                entity.setNoOfDays(t.getNoOfDays());
                entity.setTotal(t.getTotal());
                entity.setInstruction(t.getInstruction());

                // Frequency Mapping
                MasFrequency frequency = frequencyRepo.findById(t.getFrequencyId())
                        .orElseThrow(() -> new RuntimeException("Frequency not found with ID: " + t.getFrequencyId()));
                entity.setFrequency(frequency);

                // Item Mapping
                MasStoreItem item = itemRepo.findById(t.getItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found with ID: " + t.getItemId()));
                entity.setItem(item);

                entity.setTemplate(savedTemplate);
                return entity;
            }).collect(Collectors.toList());

            opdTemplateTreatmentRepository.saveAll(treatments);

            OpdTemplateResponse response = mapTemplateTreatmentToResponse(savedTemplate, treatments);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Failed to save OPD Template Treatment: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    @Transactional
    public ApiResponse<OpdTemplateResponse> updateOpdTemplateTreatment(Long templateId, OpdTemplateRequest request) {
        try {

            Long depId = authUtil.getCurrentDepartmentId();

            OpdTemplate existingTemplate = opdTempRepo.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));

            existingTemplate.setOpdTemplateName(request.getOpdTemplateName());
            existingTemplate.setOpdTemplateCode(request.getOpdTemplateCode());
//            existingTemplate.setOpdTemplateType(request.getOpdTemplateType());
            existingTemplate.setLastChgDate(Instant.now());

            MasDepartment department = departmentRepo.findById(depId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            existingTemplate.setDepartmentId(department);

            User currentUser = getCurrentUser();
            existingTemplate.setLastChgBy(currentUser.getUsername());

            // Delete old treatments
            opdTemplateTreatmentRepository.deleteAllByTemplate(existingTemplate);

            // Add new treatments
            List<OpdTemplateTreatment> newTreatments = request.getTreatments().stream().map(t -> {
                OpdTemplateTreatment entity = new OpdTemplateTreatment();
                entity.setDosage(t.getDosage());
                entity.setNoOfDays(t.getNoOfDays());
                entity.setTotal(t.getTotal());
                entity.setInstruction(t.getInstruction());

                // Frequency Mapping
                MasFrequency frequency = frequencyRepo.findById(t.getFrequencyId())
                        .orElseThrow(() -> new RuntimeException("Frequency not found with ID: " + t.getFrequencyId()));
                entity.setFrequency(frequency);

                // Item Mapping
                MasStoreItem item = itemRepo.findById(t.getItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found with ID: " + t.getItemId()));
                entity.setItem(item);

                entity.setTemplate(existingTemplate);
                return entity;
            }).collect(Collectors.toList());

            opdTemplateTreatmentRepository.saveAll(newTreatments);

            OpdTemplateResponse response = mapTemplateTreatmentToResponse(existingTemplate, newTreatments);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Failed to update OPD Template Treatment: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<List<OpdTemplateResponse>> getAllOpdTemplateTreatments(int flag) {
        try {
            List<OpdTemplate> templates;

            // Filter based on flag
            if (flag == 1) {
                templates = opdTempRepo.findByStatusIgnoreCase("Y");
            } else if (flag == 0) {
                templates = opdTempRepo.findByStatusInIgnoreCase(List.of("Y", "N"));
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value. Use 0 or 1.", 400);
            }

            // ✅ Filter templates whose type = 'p'
            templates = templates.stream()
                    .filter(t -> t.getOpdTemplateType() != null && t.getOpdTemplateType().equalsIgnoreCase("P"))
                    .collect(Collectors.toList());

            // Map to response
            List<OpdTemplateResponse> responses = templates.stream().map(template -> {
                List<OpdTemplateTreatment> treatments = opdTemplateTreatmentRepository.findByTemplate(template);
                List<OpdTemplateInvestigation> investigations = opdTempInvestRepo.findByOpdTemplateId(template);
                return mapTemplateTreatmentToResponse(template, treatments);
            }).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Failed to fetch OPD Template Treatments: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    private OpdTemplateResponse mapTemplateTreatmentToResponse(OpdTemplate template, List<OpdTemplateTreatment> treatments) {
        OpdTemplateResponse resp = new OpdTemplateResponse();
        resp.setTemplateId(template.getTemplateId());
        resp.setOpdTemplateName(template.getOpdTemplateName());
        resp.setOpdTemplateCode(template.getOpdTemplateCode());
        resp.setOpdTemplateType(template.getOpdTemplateType());
        resp.setLastChgBy(template.getLastChgBy());
        resp.setLastChgDate(template.getLastChgDate());
        resp.setStatus(template.getStatus());
        resp.setDepartmentId(template.getDepartmentId() != null ? template.getDepartmentId().getId() : null);
        resp.setDoctorId(template.getDoctorId() != null ? template.getDoctorId().getUserId() : null);

        resp.setTreatments(treatments.stream().map(t -> {
            TreatmentResponse tr = new TreatmentResponse();
            tr.setTreatmentTemp(t.getTreatmentid());
            tr.setDosage(t.getDosage());
            tr.setNoOfDays(t.getNoOfDays());
            tr.setTotal(t.getTotal());
            tr.setInstruction(t.getInstruction());
            tr.setFrequencyId(t.getFrequency() != null ? t.getFrequency().getFrequency_id() : null);
            tr.setItemId(t.getItem() != null ? t.getItem().getItemId() : null);
            tr.setDispU(t.getItem() != null ? t.getItem().getDispUnit().getUnitName() : null);
            tr.setItemName(t.getItem() != null ? t.getItem().getNomenclature() : null);
            if (t.getItem() == null) {
                tr.setStocks(0L);
            } else {

                Optional<MasStoreItem> itemOpt = masStoreItemRepository.findById(t.getItem().getItemId());

                if (itemOpt.isEmpty()) {
                    tr.setStocks(0L);
                } else {

                    MasStoreItem itemEntity = itemOpt.get();

                    List<StoreItemBatchStock> stockList =
                            storeItemBatchStockRepository.findByItemId(itemEntity);

                    if (stockList == null || stockList.isEmpty()) {
                        tr.setStocks(0L);
                    } else {

                        //filter expiryDate > today + hospDefinedDays
                        int hospDays = hospDefinedDays;
                        LocalDate threshold = LocalDate.now().plusDays(hospDays);

                        List<StoreItemBatchStock> validStockList = stockList.stream()
                                .filter(s ->
                                        s.getExpiryDate() != null &&
                                                s.getExpiryDate().isAfter(threshold)
                                )
                                .collect(Collectors.toList());

                        // sum closing stock for valid batches
                        long totalClosingStock = validStockList.stream()
                                .mapToLong(s -> s.getClosingStock() != null ? s.getClosingStock() : 0L)
                                .sum();

                        tr.setStocks(totalClosingStock);

                    }
                }
            }

            return tr;
        }).collect(Collectors.toList()));

        return resp;
    }





}
