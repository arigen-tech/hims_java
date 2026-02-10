package com.hims.controller;

import com.hims.entity.*;
import com.hims.entity.repository.MasMedicalHistoryRepository;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.*;
import com.hims.service.impl.UserDepartmentServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasterController", description = "Controller for handling All Master")
@RequestMapping("/master")
@Slf4j
public class MasterController {

    @Autowired
    private MasServiceCategoryService masServiceCategoryService;
    @Autowired
    private MasServiceOpdService masServiceOpdService;

    @Autowired
    private MasIcdService masIcdService;
    @Autowired
    private MasApplicationService masApplicationService;
    @Autowired
    private DgFixedValueService dgFixedValueService;

    @Autowired
    private MasStoreItemService masStoreItemService;
    @Autowired
    private MasBloodGroupService masBloodGroupService;
    @Autowired
    private MasCountryService masCountryService;
    @Autowired
    private MasStateService masStateService;
    @Autowired
    private MasDistrictService masDistrictService;
    @Autowired
    private MasDepartmentService masDepartmentService;
    @Autowired
    private MasDepartmentTypeService masDepartmentTypeService;
    @Autowired
    private MasGenderService masGenderService;
    @Autowired
    private MasMaritalStatusService masMaritalStatusService;
    @Autowired
    private MasRoleService masRoleService;
    @Autowired
    private MasReligionService masReligionService;
    @Autowired
    private MasRelationService masRelationService;
    @Autowired
    private DgMasSampleService dgMasSampleService;
    @Autowired
    private DgUomService dgUomService;
    @Autowired
    private MasEmploymentTypeService masEmploymentTypeService;
    @Autowired
    private MasFrequencyService masFrequencyService;
    @Autowired
    private MasIdentificationTypeService masIdentificationTypeService;
    @Autowired
    private MasItemTypeService masItemTypeService;
    @Autowired
    private MasMainChargeCodeService masMainChargeCodeService;
    @Autowired
    private MasTemplateService masTemplateService;
    @Autowired
    private MasOpdSessionService masOpdSessionService;
    @Autowired
    private MasStoreUnitService masStoreUnitService;
    @Autowired
    private MasStoreGroupService masStoreGroupService;
    @Autowired
    private MasSubChargeCodeService subService;
    @Autowired
    private MasUserTypeService masUserTypeService;
    @Autowired
    private UserDepartmentServiceImpl userDepartmentServiceImpl;
    @Autowired
    private MasHospitalService masHospitalService;
    @Autowired
    private MasStoreSectionService masStoreSectionService;
    @Autowired
    private MasItemClassService masItemClassService;
    @Autowired
    private MasItemCategoryService masItemCategoryService;
    @Autowired
    private MasHsnService masHsnService;
    @Autowired
    private MasBrandService masBrandService;
    @Autowired
    private MasManufacturerService masManufacturerService;
    @Autowired
    private DgMasCollectionService dgMasCollectionService;
    @Autowired
    private MasSymptomsService masSymptomsService;
    @Autowired
    private MasInvestigationCategoryService masInvestigationCategoryService;
    @Autowired
    private MasInvestigationMethodologyService masInvestigationMethodologyService;
    @Autowired
    private MasWardCategoryService masWardCategoryService;

    @Autowired
    private MasCareLevelService masCareLevelService;
    @Autowired
    private MasWardService masWardService;

    @Autowired
    private  MasRoomCategoryService masRoomCategoryService;
    @Autowired
    private MasBedTypeService masBedTypeService;

    @Autowired
    private  MasRoomService masRoomService;

    @Autowired
    private MasBedStatusService masBedStatusService;
    @Autowired
    private MasBedService masBedService;

    @Autowired
    private MasMedicalHistoryService masMedicalHistoryService;
    @Autowired
    private MasTreatmentAdviseService service;
    @Autowired
    private MasProcedureTypeService masProcedureTypeService;
    @Autowired
    private MasProcedureService masProcedureService;
    @Autowired
    private MasMealTypeService masMealTypeService;
    @Autowired
    private MasDietTypeService masDietTypeService;
    @Autowired
    private MasDietPreferenceService masDietPreferenceService;
    @Autowired
    private MasDietScheduleStatusService masDietScheduleStatusService;
    @Autowired
    private MasAdmissionTypeService masAdmissionTypeService;
    @Autowired
    private MasRouteService masRouteService;
    @Autowired
    private MasIntakeTypeService masIntakeTypeService;
    @Autowired
    private MasIntakeItemService masIntakeItemService;
    @Autowired
    private MasOutputTypeService masOutputTypeService;
    @Autowired
    private MasAdmissionStatusService masAdmissionStatusService;
    @Autowired
    private MasPatientAcuityService masPatientAcuityService;
    @Autowired
    private MasOpdMedicalAdviseService masOpdMedicalAdviseService;
    @Autowired
    private MasSpecialtyCenterService masSpecialtyCenterService;
    @Autowired
    private MasDesignationService masDesignationService;
    @Autowired
    private BillingPolicyService billingPolicyService;

    @Autowired
    private MasNursingTypeService masNursingTypeService;
    @Autowired
    private MasToothMasterService masToothMasterService;
    @Autowired
    private MasToothConditionService masToothConditionService;
    @Autowired
    private OpthMasDistanceVisionService opthMasDistanceVisionService;
    @Autowired
    private OphthMasNearVisionService ophthMasNearVisionService;
    @Autowired
    private  OpthMasColorVisionService opthMasColorVisionService;
    @Autowired
    private  OpthMasSpectacleUseService masSpectacleUseService;
    @Autowired
    private OpthMasLensTypeService lensTypeService;
    @Autowired
    private ObMasConceptionService obMasConceptionService;
    @Autowired
    private  ObMasConsanguinityService obMasConsanguinityService;
    @Autowired
    private ObMasBookedStatusService obMasBookedStatusService;
@Autowired
   private ObMasImmunisedStatusService obMasImmunisedStatusService;
@Autowired
private  ObMasTrimesterService obMasTrimesterService;
@Autowired
    private  ObMasPresentationService obMasPresentationService;
@Autowired
private  ObMasPvMembraneService obMasPvMembraneService;
@Autowired
private  ObMasPvLiquorService obMasPvLiquorService;
@Autowired
private ObMasCervixConsistencyService obMasCervixConsistencyService;
@Autowired
private ObMasCervixPositionService obMasCervixPositionService;
@Autowired
private ObMasStationPresentingService obMasStationPresentingService;
@Autowired
private ObMasPelvisTypeService obMasPelvisTypeService;
@Autowired
private GynMasFlowService gynMasFlowService;
@Autowired
private GynMasMenarcheAgeService gynMasMenarcheAgeService;
@Autowired
private GynMasMenstrualPatternService gynMasMenstrualPatternService;
@Autowired
private  GynMasSterilisationService gynMasSterilisationService;
@Autowired
private GynMasPapSmearService gynMasPapSmearService;
@Autowired
private EntMasPinnaService entMasPinnaService;
@Autowired
private  EntMasEarCanalService entMasEarCanalService;
@Autowired
private EntMasTmStatusService entMasTmStatusService;
@Autowired
private EntMasRinneService entMasRinneService;
@Autowired
    private EntMasWeberService entMasWeberService;
@Autowired
private  EntMasMucosaService entMasMucosaService;
@Autowired
private EntMasSeptumService entMasSeptumService;
@Autowired
private EntMasTonsilGradeService entMasTonsilGradeService;
@Autowired
private MasVaccineMasterService masVaccineMasterService;
@Autowired
private MasQuestionHeadingService masQuestionHeadingService;
@Autowired
private  MasQuestionService questionService;

@Autowired
private MasLabResultAmendmentTypeService labResultAmendmentTypeService;

@Autowired
private MasPatientPreparationService masPatientPreparationService;

@Autowired
private MasAppointmentChangeReasonService masAppointmentReasonService;
@Autowired
private MasBloodComponentService masBloodComponentService;
@Autowired
private  MasBloodDonationTypeService masBloodDonationTypeService;
@Autowired
    private MasBloodDonationStatusService masBloodDonationStatusService;
@Autowired
    private  MasBloodBagTypeService masBloodBagTypeService;
    @Autowired
    private  MasBloodCollectionTypeService masBloodCollectionTypeService;
    @Autowired
    private MasBloodInventoryStatusService masBloodInventoryStatusService;
    @Autowired
    private MasBloodUnitStatusService masBloodUnitStatusService;
    @Autowired
    private  MasBloodTestService masBloodTestService;
    @Autowired
    private MasBloodCompatibilityService masBloodCompatibilityService;

    @Autowired
    private MasCommonStatusService masCommonStatusService;
    @Autowired
    private MasLanguageService masLanguageService;




    //    ================================Mas Application Controller================================//

    @GetMapping("/mas-applications/getAll/{flag}")
    public ApiResponse<List<MasApplicationResponse>> getAllApplications(@PathVariable int flag) {
        return masApplicationService.getAllApplications(flag);
    }

    @GetMapping("/mas-applications/getById{id}")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> getApplicationById(@PathVariable String id) {
        return ResponseEntity.ok(masApplicationService.getApplicationById(id));
    }

    @PostMapping("/mas-applications/create")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> createApplication(@RequestBody MasApplicationRequest request) {
        return ResponseEntity.status(201).body(masApplicationService.createApplication(request));
    }

    @PutMapping("/mas-applications/UpdateById/{id}")
    public ResponseEntity<ApiResponse<MasApplicationResponse>> updateApplication(@PathVariable String id, @RequestBody MasApplicationRequest request) {
        return ResponseEntity.ok(masApplicationService.updateApplication(id, request));
    }

    @GetMapping("/mas-applications/getAllChildrenByParentId/{parentId}")
    public ResponseEntity<ApiResponse<List<MasApplicationResponse>>> getAllByParentId(@PathVariable String parentId, @RequestParam(required = false) Long templateId) {

        return new ResponseEntity<>(
                masApplicationService.getAllByParentId(parentId, templateId),
                HttpStatus.OK
        );
    }

    @PutMapping("/mas-applications/updateBatchStatus")
    public ResponseEntity<ApiResponse<String>> updateMultipleApplicationStatuses(@RequestBody UpdateStatusRequest request) {
        return new ResponseEntity<>(masApplicationService.updateMultipleApplicationStatuses(request), HttpStatus.OK);
    }

    @GetMapping("/mas-applications/getAllParents/{flag}")
    public ResponseEntity<ApiResponse<List<MasApplicationResponse>>> getAllParentApplications(@PathVariable int flag) {
        return new ResponseEntity<>(masApplicationService.getAllParentApplications(flag), HttpStatus.OK);
    }

    @PostMapping("/mas-applications/assignUpdateTemplate")
    public ResponseEntity<ApiResponse<String>> processBatchUpdates(@RequestBody BatchUpdateRequest request) {
        ApiResponse<String> response = masApplicationService.processBatchUpdates(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================Mas BloodGroup Controller================================//

    @GetMapping("/blood-group/getAll/{flag}")
    public ApiResponse<List<MasBloodGroupResponse>> getAllBloodGroups(@PathVariable int flag) {
        return masBloodGroupService.getAllBloodGroups(flag);
    }

    @GetMapping("/blood-group/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodGroupResponse>> getBloodGroupById(@PathVariable Long id) {
        ApiResponse<MasBloodGroupResponse> response = masBloodGroupService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/blood-group/create")
    public ResponseEntity<ApiResponse<MasBloodGroupResponse>> addBloodGroup(@RequestBody MasBloodGroupRequest bloodGroupRequest) {
        ApiResponse<MasBloodGroupResponse> response = masBloodGroupService.addBloodGroup(bloodGroupRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/blood-group/updateById/{id}")
    public ResponseEntity<ApiResponse<MasBloodGroupResponse>> updateBloodGroup(@PathVariable Long id, @RequestBody MasBloodGroupRequest bloodGroupRequest) {
        ApiResponse<MasBloodGroupResponse> response = masBloodGroupService.updateBloodGroup(id, bloodGroupRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/blood-group/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodGroupResponse>> changeBloodGroupStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasBloodGroupResponse> response = masBloodGroupService.changeBloodGroupStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================Mas Countries Controller================================//

    @GetMapping("/country/getAll/{flag}")
    public ApiResponse<List<MasCountryResponse>> getAllCountries(@PathVariable int flag) {
        return masCountryService.getAllCountries(flag);
    }

    @PostMapping("/country/create")
    public ApiResponse<MasCountryResponse> addCountry(@RequestBody MasCountryRequest request) {
        return masCountryService.addCountry(request);
    }

    @PutMapping("/country/status/{id}")
    public ApiResponse<String> changeCountryStatus(@PathVariable Long id, @RequestParam String status) {
        return masCountryService.changeCountryStatus(id, status);
    }

    @PutMapping("/country/updateById/{id}")
    public ApiResponse<MasCountryResponse> editCountry(@PathVariable Long id, @RequestBody MasCountryRequest request) {
        return masCountryService.editCountry(id, request);
    }

    @GetMapping("/country/getById/{id}")
    public ApiResponse<MasCountryResponse> getCountryById(@PathVariable Long id) {
        return masCountryService.getCountryById(id);
    }


    //    ================================Mas States Controller================================//

    @GetMapping("/state/getAll/{flag}")
    public ApiResponse<List<MasStateResponse>> getAllStates(@PathVariable int flag) {
        return masStateService.getAllStates(flag);
    }

    @PostMapping("/state/create")
    public ApiResponse<MasStateResponse> addState(@RequestBody MasStateRequest request) {
        return masStateService.addState(request);
    }

    @PutMapping("/state/status/{id}")
    public ApiResponse<String> changeStateStatus(@PathVariable Long id, @RequestParam String status) {
        return masStateService.changeStateStatus(id, status);
    }

    @PutMapping("/state/updateById/{id}")
    public ApiResponse<MasStateResponse> editState(@PathVariable Long id, @RequestBody MasStateRequest request) {
        return masStateService.editState(id, request);
    }

    @GetMapping("/state/getById{id}")
    public ApiResponse<MasStateResponse> getStateById(@PathVariable Long id) {
        return masStateService.getStateById(id);
    }

    @GetMapping("/state/getByCountryId/{countryId}")
    public ApiResponse<List<MasStateResponse>> getStatesByCountryId(@PathVariable Long countryId) {
        return masStateService.getStatesByCountryId(countryId);
    }


    //    ================================Mas Districts Controller================================//

    @GetMapping("/district/getAll/{flag}")
    public ApiResponse<List<MasDistrictResponse>> getAllDistricts(@PathVariable int flag) {
        return masDistrictService.getAllDistricts(flag);
    }

    @PostMapping("/district/create")
    public ResponseEntity<ApiResponse<MasDistrictResponse>> addDistrict(@RequestBody MasDistrictRequest request) {
        return ResponseEntity.ok(masDistrictService.addDistrict(request));
    }

    @PutMapping("/district/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeDistrictStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(masDistrictService.changeDistrictStatus(id, status));
    }

    @PutMapping("/district/updateById/{id}")
    public ResponseEntity<ApiResponse<MasDistrictResponse>> editDistrict(@PathVariable Long id, @RequestBody MasDistrictRequest request) {
        return ResponseEntity.ok(masDistrictService.editDistrict(id, request));
    }

    @GetMapping("/district/getById/{id}")
    public ResponseEntity<ApiResponse<MasDistrictResponse>> getDistrictById(@PathVariable Long id) {
        return ResponseEntity.ok(masDistrictService.getDistrictById(id));
    }

    @GetMapping("/district/getByState/{stateId}")
    public ResponseEntity<ApiResponse<List<MasDistrictResponse>>> getDistrictsByStateId(@PathVariable Long stateId) {
        return ResponseEntity.ok(masDistrictService.getDistrictsByStateId(stateId));
    }


    //    ================================Mas Departments Controller================================//

    @PostMapping("/department/create")
    public ResponseEntity<ApiResponse<MasDepartmentResponse>> addDepartment(@RequestBody MasDepartmentRequest request) {
        return ResponseEntity.ok(masDepartmentService.addDepartment(request));
    }

    @PutMapping("/department/updateById/{id}")
    public ResponseEntity<ApiResponse<MasDepartmentResponse>> editDepartment(@PathVariable Long id, @RequestBody MasDepartmentRequest request) {
        return ResponseEntity.ok(masDepartmentService.editDepartment(id, request));
    }

    @PutMapping("/department/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeDepartmentStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(masDepartmentService.changeDepartmentStatus(id, status));
    }

    @GetMapping("/department/getById/{id}")
    public ResponseEntity<ApiResponse<MasDepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(masDepartmentService.getDepartmentById(id));
    }

    @GetMapping("/department/getAll/{flag}")
    public ApiResponse<List<MasDepartmentResponse>> getAllDepartments(@PathVariable int flag) {
        return masDepartmentService.getAllDepartments(flag);
    }

    @GetMapping("/department/getAllUserDepartment")
    public ResponseEntity<ApiResponse<List<MasUserDepartmentResponse>>> getAllUserDepartments() {
        return ResponseEntity.ok(masDepartmentService.getAllMasUserDepartments());
    }

    @GetMapping("/department/getUserDepartmentsByDepartmentId/{departmentId}")
    public ResponseEntity<ApiResponse<List<MasUserDepartmentResponse>>> getUserDepartmentsByDepartmentId(@PathVariable Long departmentId) {
        return ResponseEntity.ok(masDepartmentService.getMasUserDepartmentsByDepartmentId(departmentId));
    }

    @GetMapping("/department/getUserDepartmentsByUserId/{userId}")
    public ResponseEntity<ApiResponse<List<MasUserDepartmentResponse>>> getUserDepartmentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(masDepartmentService.getMasUserDepartmentsByUserId(userId));
    }

    @GetMapping("/ward-department/getAllBy/{wardCategory}")
    public ResponseEntity<ApiResponse<List<MasDepartmentResponse>>> getAllWardDepartmentByWardCategory(@PathVariable Long wardCategory) {
        return ResponseEntity.ok(masDepartmentService.getAllWardDepartmentByWardCategory(wardCategory));
    }

    @GetMapping("/indent-department/getAll")
    public ResponseEntity<?> getAllIndentDepartment(@RequestParam String status) {
        return ResponseEntity.ok(masDepartmentService.getAllIndentApplicableDepartments(status));
    }

    //    ================================Mas DepartmentType Controller================================//

    @PostMapping("/department-type/create")
    public ResponseEntity<ApiResponse<MasDepartmentTypeResponse>> addDepartmentType(@RequestBody MasDepartmentTypeRequest request) {
        return ResponseEntity.ok(masDepartmentTypeService.addDepartmentType(request));
    }

    @PutMapping("/department-type/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeDepartmentTypeStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(masDepartmentTypeService.changeDepartmentTypeStatus(id, status));
    }

    @PutMapping("/department-type/updateById/{id}")
    public ResponseEntity<ApiResponse<MasDepartmentTypeResponse>> editDepartmentType(@PathVariable Long id, @RequestBody MasDepartmentTypeRequest request) {
        return ResponseEntity.ok(masDepartmentTypeService.editDepartmentType(id, request));
    }

    @GetMapping("/department-type/getById{id}")
    public ResponseEntity<ApiResponse<MasDepartmentTypeResponse>> getDepartmentTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(masDepartmentTypeService.getDepartmentTypeById(id));
    }

    @GetMapping("/department-type/getAll/{flag}")
    public ApiResponse<List<MasDepartmentTypeResponse>> getAllDepartmentTypes(@PathVariable int flag) {
        return masDepartmentTypeService.getAllDepartmentTypes(flag);
    }


    //    ================================Mas Genders Controller================================//

    @GetMapping("/gender/getAll/{flag}")
    public ApiResponse<List<MasGenderResponse>> getAllGenders(@PathVariable int flag) {
        return masGenderService.getAllGenders(flag);
    }

    @GetMapping("/gender/getById{id}")
    public ResponseEntity<ApiResponse<MasGenderResponse>> getGenderById(@PathVariable Long id) {
        ApiResponse<MasGenderResponse> response = masGenderService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/gender/create")
    public ResponseEntity<ApiResponse<MasGenderResponse>> addGender(@RequestBody MasGenderRequest genderRequest) {
        ApiResponse<MasGenderResponse> response = masGenderService.addGender(genderRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/gender/updateById/{id}")
    public ResponseEntity<ApiResponse<MasGenderResponse>> updateGender(@PathVariable Long id, @RequestBody MasGenderResponse genderDetails) {
        ApiResponse<MasGenderResponse> response = masGenderService.updateGender(id, genderDetails);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/gender/status/{id}")
    public ResponseEntity<ApiResponse<MasGenderResponse>> changeGenderStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasGenderResponse> response = masGenderService.changeGenderStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================Mas Marital Status Controller================================//

    @PostMapping("/marital-status/create")
    public ApiResponse<MasMaritalStatusResponse> addMaritalStatus(@RequestBody MasMaritalStatusRequest request) {
        return masMaritalStatusService.addMaritalStatus(request);
    }

    @PutMapping("/marital-status/status/{id}")
    public ApiResponse<String> changeMaritalStatus(@PathVariable Long id, @RequestParam String status) {
        return masMaritalStatusService.changeMaritalStatus(id, status);
    }

    @PutMapping("/marital-status/updateById/{id}")
    public ApiResponse<MasMaritalStatusResponse> editMaritalStatus(@PathVariable Long id, @RequestBody MasMaritalStatusRequest request) {
        return masMaritalStatusService.editMaritalStatus(id, request);
    }

    @GetMapping("/marital-status/getById{id}")
    public ApiResponse<MasMaritalStatusResponse> getMaritalStatusById(@PathVariable Long id) {
        return masMaritalStatusService.getMaritalStatusById(id);
    }

    @GetMapping("/marital-status/getAll/{flag}")
    public ApiResponse<List<MasMaritalStatusResponse>> getAllMaritalStatuses(@PathVariable int flag) {
        return masMaritalStatusService.getAllMaritalStatuses(flag);
    }


    //    ================================Mas Roles Controller================================//

    @PostMapping("/roles/create")
    public ApiResponse<MasRoleResponse> addRole(@RequestBody MasRoleRequest request) {
        return masRoleService.addRole(request);
    }

    @PutMapping("/roles/status/{id}")
    public ApiResponse<String> changeRoleStatus(@PathVariable Long id, @RequestParam String status) {
        return masRoleService.changeRoleStatus(id, status);
    }

    @PutMapping("/roles/updateById/{id}")
    public ApiResponse<MasRoleResponse> editRole(@PathVariable Long id, @RequestBody MasRoleRequest request) {
        return masRoleService.editRole(id, request);
    }

    @GetMapping("/roles/getById/{id}")
    public ApiResponse<MasRoleResponse> getRoleById(@PathVariable Long id) {
        return masRoleService.getRoleById(id);
    }

    @GetMapping("/roles/getAll/{flag}")
    public ApiResponse<List<MasRoleResponse>> getAllRoles(@PathVariable int flag) {
        return masRoleService.getAllRoles(flag);
    }


    //    ================================Mas Religion Controller================================//

    @GetMapping("/religion/getAll/{flag}")
    public ApiResponse<List<MasReligionResponse>> getAllReligions(@PathVariable int flag) {
        return masReligionService.getAllReligions(flag);
    }

    @GetMapping("/religion/getById{id}")
    public ResponseEntity<ApiResponse<MasReligionResponse>> getReligionById(@PathVariable Long id) {
        ApiResponse<MasReligionResponse> response = masReligionService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/religion/create")
    public ResponseEntity<ApiResponse<MasReligionResponse>> addReligion(@RequestBody MasReligionRequest religionRequest) {
        ApiResponse<MasReligionResponse> response = masReligionService.addReligion(religionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/religion/updateById/{id}")
    public ResponseEntity<ApiResponse<MasReligionResponse>> updateReligion(@PathVariable Long id, @RequestBody MasReligionRequest religionRequest) {
        ApiResponse<MasReligionResponse> response = masReligionService.updateReligion(id, religionRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/religion/status/{id}")
    public ResponseEntity<ApiResponse<MasReligionResponse>> changeReligionStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasReligionResponse> response = masReligionService.changeReligionStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================Mas Relations Controller================================//

    @GetMapping("/relation/getAll/{flag}")
    public ApiResponse<List<MasRelationResponse>> getAllRelations(@PathVariable int flag) {
        return masRelationService.getAllRelations(flag);
    }

    @GetMapping("/relation/getById/{id}")
    public ResponseEntity<ApiResponse<MasRelationResponse>> getRelationById(@PathVariable Long id) {
        ApiResponse<MasRelationResponse> response = masRelationService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/relation/create")
    public ResponseEntity<ApiResponse<MasRelationResponse>> addRelation(@RequestBody MasRelationRequest relationRequest) {
        ApiResponse<MasRelationResponse> response = masRelationService.addRelation(relationRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/relation/updateById/{id}")
    public ResponseEntity<ApiResponse<MasRelationResponse>> updateRelation(@PathVariable Long id, @RequestBody MasRelationRequest relationRequest) {
        ApiResponse<MasRelationResponse> response = masRelationService.updateRelation(id, relationRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/relation/status/{id}")
    public ResponseEntity<ApiResponse<MasRelationResponse>> changeRelationsStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasRelationResponse> response = masRelationService.changeRelationsStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================DgMasSample Controller================================//

    @PostMapping("/dg-mas-sample/create")
    public ResponseEntity<ApiResponse<DgMasSampleResponse>> addDgMasSample(@RequestBody DgMasSampleRequest dgMasSampleRequest) {
        return new ResponseEntity<>(dgMasSampleService.addDgMasSample(dgMasSampleRequest), HttpStatus.CREATED);
    }

    @GetMapping("/dg-mas-sample/getById/{id}")
    public ResponseEntity<ApiResponse<DgMasSampleResponse>> getByIdDgMasSample(@PathVariable Long id) {
        return new ResponseEntity<>(dgMasSampleService.getByIdDgMas(id), HttpStatus.OK);
    }

    @GetMapping("/dg-mas-sample/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<DgMasSampleResponse>>> getAllDgMasSample(@PathVariable int flag) {
        return new ResponseEntity<>(dgMasSampleService.getAllDgMas(flag), HttpStatus.OK);
    }

    @PutMapping("/dg-mas-sample/status/{id}")
    public ResponseEntity<ApiResponse<DgMasSampleResponse>> updateByStatusDgMas(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(dgMasSampleService.updateByStatusDgMas(id, status), HttpStatus.OK);

    }

    @PutMapping("/dg-mas-sample/updateById/{id}")
    public ResponseEntity<ApiResponse<DgMasSampleResponse>> updateByIdDgMas(@PathVariable Long id, @RequestBody DgMasSampleRequest dgMasSampleRequest) {
        return new ResponseEntity<>(dgMasSampleService.updateByIdDgMas(id, dgMasSampleRequest), HttpStatus.OK);

    }


    //    ================================DgUom Controller================================//

    @PostMapping("/dgUom/create")
    public ResponseEntity<ApiResponse<DgUomResponse>> addDgUom(@RequestBody DgUomRequest dgUomRequest) {
        return new ResponseEntity<>(dgUomService.addDgUom(dgUomRequest), HttpStatus.CREATED);
    }

    @GetMapping("/dgUom/getById/{id}")
    public ResponseEntity<ApiResponse<DgUomResponse>> getByIdDgUom(@PathVariable Long id) {
        return new ResponseEntity<>(dgUomService.getByIdDgUom(id), HttpStatus.OK);
    }

    @GetMapping("/dgUom/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<DgUomResponse>>> getAllDgUom(@PathVariable int flag) {
        return new ResponseEntity<>(dgUomService.getAllDgUom(flag), HttpStatus.OK);
    }

    @PutMapping("/dgUom/status/{id}")
    public ResponseEntity<ApiResponse<DgUomResponse>> updateByStatusDgUom(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(dgUomService.updateByStatusDgUom(id, status), HttpStatus.OK);

    }

    @PutMapping("/dgUom/updateById/{id}")
    public ResponseEntity<ApiResponse<DgUomResponse>> updateByIdDgUom(@PathVariable Long id, @RequestBody DgUomRequest dgUomRequest) {
        return new ResponseEntity<>(dgUomService.updateByIdDgUom(id, dgUomRequest), HttpStatus.OK);

    }


    //    ================================EmployeementType Controller================================//

    @GetMapping("/employmentType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasEmploymentTypeResponse>>> getAllMasEmploymentType(@PathVariable int flag) {
        return new ResponseEntity<>(masEmploymentTypeService.getAllMasEmploymentType(flag), HttpStatus.OK);

    }

    @GetMapping("/employmentType/getById/{id}")
    public ResponseEntity<ApiResponse<MasEmploymentTypeResponse>> getMasEmploymentTypeById(@PathVariable Long id) {
        return new ResponseEntity<>(masEmploymentTypeService.getMasEmploymentTypeId(id), HttpStatus.OK);

    }

    @PostMapping("/employmentType/create")
    public ResponseEntity<ApiResponse<MasEmploymentTypeResponse>> addMasEmploymentType(@RequestBody MasEmploymentTypeRequest masEmploymentTypeRequest) {
        return new ResponseEntity<>(masEmploymentTypeService.addMasEmploymentType(masEmploymentTypeRequest), HttpStatus.CREATED);
    }

    @PutMapping("/employmentType/updateById/{id}")
    public ResponseEntity<ApiResponse<MasEmploymentTypeResponse>> updateMasEmploymentTypeById(@RequestBody MasEmploymentType masEmploymentType, @PathVariable Long id) {
        return new ResponseEntity<>(masEmploymentTypeService.updateMasEmploymentTypeById(masEmploymentType, id), HttpStatus.OK);
    }

    @PutMapping("/employmentType/status/{id}/{status}")
    public ResponseEntity<ApiResponse<MasEmploymentTypeResponse>> updateMasEmploymentTypeByStatus(@PathVariable Long id, @PathVariable String status) {
        return new ResponseEntity<>(masEmploymentTypeService.updateMasEmploymentTypeByStatus(id, status), HttpStatus.OK);
    }


    //    ================================Mas Frequency Controller================================//

    @PostMapping("/masFrequency/create")
    public ResponseEntity<ApiResponse<MasFrequencyResponse>> createMasFrequency(@RequestBody MasFrequencyRequest masFrequencyRequest) {
        return new ResponseEntity<>(masFrequencyService.createMasFrequency(masFrequencyRequest), HttpStatus.CREATED);

    }

    @PutMapping("/masFrequency/updateById/{id}")
    public ResponseEntity<ApiResponse<MasFrequencyResponse>> updateMasFrequency(@PathVariable Long id, @RequestBody MasFrequencyRequest masFrequencyRequest) {
        return new ResponseEntity<>(masFrequencyService.updateMasFrequency(id, masFrequencyRequest), HttpStatus.OK);

    }

    @PutMapping("/masFrequency/status/{id}/{status}")
    public ResponseEntity<ApiResponse<MasFrequencyResponse>> updateMasFrequencyByStatus(@PathVariable Long id, @PathVariable String status) {
        return new ResponseEntity<>(masFrequencyService.updateMasFrequencyByStatus(id, status), HttpStatus.OK);

    }

    @GetMapping("/masFrequency/getById/{id}")
    public ResponseEntity<ApiResponse<MasFrequencyResponse>> getByIdMasFrequency(@PathVariable Long id) {
        return new ResponseEntity<>(masFrequencyService.getByIdMasFrequency(id), HttpStatus.OK);

    }

    @GetMapping("/masFrequency/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasFrequencyResponse>>> getByMasFrequency(@PathVariable int flag) {
        return new ResponseEntity<>(masFrequencyService.getByAllMasFrequency(flag), HttpStatus.OK);

    }


    //    ================================IdentificationType Controller================================//

    @PostMapping("/identification-types/create")
    public ApiResponse<MasIdentificationTypeResponse> addIdentificationType(@RequestBody MasIdentificationTypeRequest request) {
        return masIdentificationTypeService.addIdentificationType(request);
    }

    @PutMapping("/identification-types/status/{id}")
    public ApiResponse<String> changeIdentificationStatus(@PathVariable Long id, @RequestParam String status) {
        return masIdentificationTypeService.changeIdentificationStatus(id, status);
    }

    @PutMapping("/identification-types/updateById/{id}")
    public ApiResponse<MasIdentificationTypeResponse> editIdentificationType(@PathVariable Long id, @RequestBody MasIdentificationTypeRequest request) {
        return masIdentificationTypeService.editIdentificationType(id, request);
    }

    @GetMapping("/identification-types/getById/{id}")
    public ApiResponse<MasIdentificationTypeResponse> getIdentificationTypeById(@PathVariable Long id) {
        return masIdentificationTypeService.getIdentificationTypeById(id);
    }

    @GetMapping("/identification-types/getAll/{flag}")
    public ApiResponse<List<MasIdentificationTypeResponse>> getAllIdentificationTypes(@PathVariable int flag) {
        return masIdentificationTypeService.getAllIdentificationTypes(flag);
    }


    //    ================================Mas ItemType Controller================================//

    @PostMapping("/masItemType/create")
    public ResponseEntity<ApiResponse<MasItemTypeResponse>> addMasTypeItem(@RequestBody MasItemTypeRequest masItemTypeRequest) {
        return new ResponseEntity<>(masItemTypeService.addMasItemType(masItemTypeRequest), HttpStatus.CREATED);

    }

    @PutMapping("/masItemType/updateById/{id}")
    public ResponseEntity<ApiResponse<MasItemTypeResponse>> updateMasTypeItemID(@PathVariable int id, @RequestBody MasItemTypeRequest masItemTypeRequest) {
        return new ResponseEntity<>(masItemTypeService.updateMasItemTypeID(id, masItemTypeRequest), HttpStatus.OK);

    }

    @PutMapping("/masItemType/status/{id}")
    public ResponseEntity<ApiResponse<MasItemTypeResponse>> updateMasTypeItemStatus(@PathVariable int id, @RequestParam String status) {
        return new ResponseEntity<>(masItemTypeService.updateMasItemTypeStatus(id, status), HttpStatus.OK);

    }

    @GetMapping("/masItemType/getById/{id}")
    public ResponseEntity<ApiResponse<MasItemTypeResponse>> getByMasTypeItemStatus(@PathVariable int id) {
        return new ResponseEntity<>(masItemTypeService.getByMasItemTypeStatus(id), HttpStatus.OK);

    }

    @GetMapping("/masItemType/getByAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasItemTypeResponse>>> getByAllMasTypeItemStatus(@PathVariable int flag) {
        return new ResponseEntity<>(masItemTypeService.getAllMasItemTypeStatus(flag), HttpStatus.OK);

    }
    @GetMapping("/masItemType/findByGroupId/{id}")
    public ResponseEntity<ApiResponse<List<MasItemTypeResponse>>> findItemType(@PathVariable Long id) {
        return new ResponseEntity<>(masItemTypeService.findItemType(id), HttpStatus.OK);

    }



    //    ================================Mas MainChargeCode Controller================================//

    @GetMapping("/main-charge-code/getAll/{flag}")
    public ApiResponse<List<MasMainChargeCodeDTO>> getAllChargeCode(@PathVariable int flag) {
        return masMainChargeCodeService.getAllChargeCode(flag);
    }

    @GetMapping("/main-charge-code/getById/{chargecodeId}")
    public ResponseEntity<ApiResponse<MasMainChargeCodeDTO>> getChargeCodeById(@PathVariable Long chargecodeId) {
        return new ResponseEntity<>(masMainChargeCodeService.getChargeCodeById(chargecodeId), HttpStatus.OK);
    }

    @PostMapping("/main-charge-code/create")
    public ResponseEntity<ApiResponse<MasMainChargeCodeDTO>> createChargeCode(@RequestBody MasMainChargeCodeRequest codeRequest) {
        return new ResponseEntity<>(masMainChargeCodeService.createChargeCode(codeRequest), HttpStatus.CREATED);
    }

    @PutMapping("/main-charge-code/updateById/{chargecodeId}")
    public ResponseEntity<ApiResponse<MasMainChargeCodeDTO>> updateChargeCode(@PathVariable Long chargecodeId, @RequestBody MasMainChargeCodeRequest codeRequest) {
        return new ResponseEntity<>(masMainChargeCodeService.updateChargeCode(chargecodeId, codeRequest), HttpStatus.ACCEPTED);
    }

    @PutMapping("/main-charge-code/status/{chargecodeId}")
    public ResponseEntity<ApiResponse<MasMainChargeCodeDTO>> changeMainChargeCodeStatus(@PathVariable Long chargecodeId, @RequestParam String status) {
        return new ResponseEntity<>(masMainChargeCodeService.changeMainChargeCodeStatus(chargecodeId, status), HttpStatus.ACCEPTED);
    }


    //    ================================Mas Templates Controller================================//

    @GetMapping("/mas-templates/getAll/{flag}")
    public ApiResponse<List<MasTemplateResponse>> getAllTemplates(@PathVariable int flag) {
        return masTemplateService.getAllTemplates(flag);
    }

    @GetMapping("/mas-templates/getById/{id}")
    public ResponseEntity<ApiResponse<MasTemplateResponse>> getTemplateById(@PathVariable Long id) {
        return new ResponseEntity<>(masTemplateService.getTemplateById(id), HttpStatus.OK);
    }

    @PostMapping("/mas-templates/create")
    public ResponseEntity<ApiResponse<MasTemplateResponse>> createTemplate(@RequestBody MasTemplateRequest request) {
        return new ResponseEntity<>(masTemplateService.createTemplate(request), HttpStatus.CREATED);
    }

    @PutMapping("/mas-templates/updateById/{id}")
    public ResponseEntity<ApiResponse<MasTemplateResponse>> updateTemplate(@PathVariable Long id, @RequestBody MasTemplateRequest request) {
        return new ResponseEntity<>(masTemplateService.updateTemplate(id, request), HttpStatus.OK);
    }

    @PutMapping("/mas-templates/status/{id}")
    public ResponseEntity<ApiResponse<String>> changeTemplateStatus(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(masTemplateService.changeTemplateStatus(id, status), HttpStatus.OK);
    }


    //    ================================Mas OpdSession Controller================================//

    @GetMapping("/opd-session/getAll/{flag}")
    public ApiResponse<List<MasOpdSessionResponse>> getAllOpdSessions(@PathVariable int flag) {
        return masOpdSessionService.getAllOpdSessions(flag);
    }

    @GetMapping("/opd-session/getById/{id}")
    public ResponseEntity<ApiResponse<MasOpdSessionResponse>> getOpdSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(masOpdSessionService.findById(id));
    }

    @PostMapping("/opd-session/create")
    public ResponseEntity<ApiResponse<MasOpdSessionResponse>> addSession(@RequestBody MasOpdSessionRequest request) {
        ApiResponse<MasOpdSessionResponse> response = masOpdSessionService.addSession(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/opd-session/updateById/{id}")
    public ResponseEntity<ApiResponse<MasOpdSessionResponse>> updateSession(@PathVariable Long id, @RequestBody MasOpdSessionRequest request) {
        ApiResponse<MasOpdSessionResponse> response = masOpdSessionService.updateSession(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/opd-session/status/{id}")
    public ResponseEntity<ApiResponse<MasOpdSessionResponse>> changeOpdSessionStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(masOpdSessionService.changeOpdSessionStatus(id, status));
    }


    //    ================================Mas StoreUnit Controller================================//

    @GetMapping("/store-unit/getAll/{flag}")
    public ApiResponse<List<MasStoreUnitResponse>> getAllUnits(@PathVariable int flag) {
        return masStoreUnitService.getAllUnits(flag);
    }

    @GetMapping("/store-unit/getById/{unit_id}")
    public ResponseEntity<ApiResponse<MasStoreUnitResponse>> findByUnit(@PathVariable Long unit_id) {
        return ResponseEntity.ok(masStoreUnitService.findByUnit(unit_id));
    }

    @PostMapping("/store-unit/create")
    public ResponseEntity<ApiResponse<MasStoreUnitResponse>> addUnit(@RequestBody MasStoreUnitRequest unitRequest) {
        ApiResponse<MasStoreUnitResponse> response = masStoreUnitService.addUnit(unitRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/store-unit/updateById/{unit_id}")
    public ResponseEntity<ApiResponse<MasStoreUnitResponse>> updateUnit(@PathVariable Long unit_id, @RequestBody MasStoreUnitRequest unitRequest) {
        ApiResponse<MasStoreUnitResponse> response = masStoreUnitService.updateUnit(unit_id, unitRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/store-unit/status/{unit_id}")
    public ResponseEntity<ApiResponse<MasStoreUnitResponse>> changeStat(@PathVariable Long unit_id, @RequestParam String stat) {
        return ResponseEntity.ok(masStoreUnitService.changeStat(unit_id, stat));
    }


    //    ================================Mas Application Controller================================//

    @GetMapping("/masStoreGroup/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasStoreGroupResponse>>> getMasStoreGroupByAllId(@PathVariable int flag) {
        return new ResponseEntity<>(masStoreGroupService.getMasStoreGroupAllId(flag), HttpStatus.OK);
    }

    @GetMapping("/masStoreGroup/getById/{id}")
    public ResponseEntity<ApiResponse<MasStoreGroupResponse>> getMasStoreGroupById(@RequestParam int id) {
        return new ResponseEntity<>(masStoreGroupService.getMasStoreGroup(id), HttpStatus.OK);
    }

    @PostMapping("/masStoreGroup/create")
    public ResponseEntity<ApiResponse<MasStoreGroupResponse>> addMasStoreGroup(@RequestBody MasStoreGroupRequest masStoreGroupRequest) {
        return new ResponseEntity<>(masStoreGroupService.addMasStoreGroup(masStoreGroupRequest), HttpStatus.CREATED);
    }

    @PutMapping("/masStoreGroup/updateById/{id}")
    public ResponseEntity<ApiResponse<MasStoreGroupResponse>> updateMasStoreGroup(@RequestParam int id, @RequestBody MasStoreGroup masStoreGroup) {
        return new ResponseEntity<>(masStoreGroupService.updateMasStoreGroup(id, masStoreGroup), HttpStatus.OK);
    }

    @PutMapping("/masStoreGroup/status/{id}/{status}")
    public ResponseEntity<ApiResponse<MasStoreGroupResponse>> updateStatusMasStoreGroup(@RequestParam int id, @RequestParam String status) {
        return new ResponseEntity<>(masStoreGroupService.updateStatusMasStoreGroup(id, status), HttpStatus.OK);
    }


    //    ================================Mas SubCharge Controller================================//

    @PostMapping("/sub-charge-code/create")
    ResponseEntity<ApiResponse<MasSubChargeCodeDTO>> createSubCharge(@RequestBody MasSubChargeCodeReq codeReq) {
        return new ResponseEntity<>(subService.createSubCharge(codeReq), HttpStatus.CREATED);
    }

    @PutMapping("/sub-charge-code/updateById/{subId}")
    ResponseEntity<ApiResponse<MasSubChargeCodeDTO>> updateSubCharge(@PathVariable Long subId, @RequestBody MasSubChargeCodeReq codeReq) {
        return new ResponseEntity<>(subService.updateSubCharge(subId, codeReq), HttpStatus.ACCEPTED);
    }

    @PutMapping("/sub-charge-code/status/{subId}")
    ResponseEntity<ApiResponse<MasSubChargeCodeDTO>> changeSubChargeStatus(@PathVariable Long subId, @RequestParam String status) {
        return new ResponseEntity<>(subService.changeSubChargeStatus(subId, status), HttpStatus.ACCEPTED);
    }

    @GetMapping("/sub-charge-code/getById/{subId}")
    ResponseEntity<ApiResponse<MasSubChargeCodeDTO>> getBySubId(@PathVariable Long subId) {
        return new ResponseEntity<>(subService.getBySubId(subId), HttpStatus.OK);
    }

    @GetMapping("/sub-charge-code/getAll/{flag}")
    ResponseEntity<ApiResponse<List<MasSubChargeCodeDTO>>> getAllSubCharge(@PathVariable int flag) {
        return new ResponseEntity<>(subService.getAllSubCharge(flag), HttpStatus.OK);
    }


    //    ================================Mas UserType Controller================================//

    @GetMapping("/userType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasUserType>>> getAll(@PathVariable int flag) {
        return new ResponseEntity<>(masUserTypeService.getAllMasUserType(flag), HttpStatus.OK);
    }

    @GetMapping("/userType/getById/{id}")
    public ResponseEntity<ApiResponse<MasUserTypeResponse>> getByIdMasUserType(@PathVariable Long id) {
        return new ResponseEntity<>(masUserTypeService.getByIdMasUserType(id), HttpStatus.OK);
    }

    @PostMapping("/userType/create")
    public ResponseEntity<ApiResponse<MasUserTypeResponse>> newAddMasUser(@RequestBody MasUserTypeRequest masUserTypeRequest) {
        return new ResponseEntity<>(masUserTypeService.newAddMasUser(masUserTypeRequest), HttpStatus.OK);
    }

    @PutMapping("/userType/updateById/{id}")
    public ResponseEntity<ApiResponse<MasUserTypeResponse>> updateMasUserType(@RequestBody MasUserType masUserType, @PathVariable Long id) {
        return new ResponseEntity<>(masUserTypeService.updateMasUserType(masUserType, id), HttpStatus.OK);
    }

    @PutMapping("/userType/status/{id}/{status}")
    public ResponseEntity<ApiResponse<MasUserTypeResponse>> updateMasUserTypeStatus(@PathVariable Long id, @PathVariable String status) {
        return new ResponseEntity<>(masUserTypeService.updateMasUserTypeStatus(id, status), HttpStatus.OK);
    }


    //    ================================Mas UserDepartment Controller================================//

    @GetMapping("/user-departments/getAll")
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartment() {
        return userDepartmentServiceImpl.getAllUserDepartments();
    }

    @PostMapping("/user-departments/create")
    public ApiResponse<UserDepartmentResponse> addUserDepartment(@RequestBody UserDepartmentRequest request) {
        return userDepartmentServiceImpl.addUserDepartment(request);
    }

    @PutMapping("/user-departments/updateById/{id}")
    public ApiResponse<UserDepartmentResponse> updateUserDepartment(@PathVariable Long id, @RequestBody UserDepartmentResponse details) {
        return userDepartmentServiceImpl.updateUserDepartment(id, details);
    }

    @GetMapping("/user-departments/getById{id}")
    public ApiResponse<UserDepartmentResponse> findById(@PathVariable Long id) {
        return userDepartmentServiceImpl.findById(id);
    }

    @GetMapping("/user-departments/getByUserId/{id}")
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartmentsByUserId(@PathVariable Long id) {
        return userDepartmentServiceImpl.getAllUserDepartmentsByUserId(id);
    }

    @GetMapping("/user-departments/getByUserName/{userName}")
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartmentsByUserUserName(@PathVariable String userName) {
        return userDepartmentServiceImpl.getAllUserDepartmentsByUserUserName(userName);
    }

    @PutMapping("/user-departments/addOrUpdateUserDept")
    public ResponseEntity<ApiResponse<String>> addOrUpdateUserDepartment(@RequestBody UserDepartmentRequestOne request) {
        ApiResponse<String> response = userDepartmentServiceImpl.addOrUpdateUserDept(request);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }


    //    ================================Mas Hospital Controller================================//

    @GetMapping("/hospital/getAll/{flag}")
    public ApiResponse<List<MasHospitalResponse>> getAllHospitals(@PathVariable int flag) {
        return masHospitalService.getAllHospitals(flag);
    }
    @GetMapping("/hospitalResponse/getAll/{flag}")
    public ApiResponse<List<MasHospitalResponse2>> getAllHospitalsResponse(@PathVariable int flag) {
        return masHospitalService.getAllHospitalsResponse(flag);
    }

    @GetMapping("/hospital/getById/{id}")
    public ResponseEntity<ApiResponse<MasHospitalResponse>> getHospitalById(@PathVariable Long id) {
        ApiResponse<MasHospitalResponse> response = masHospitalService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/hospital/create")
    public ResponseEntity<ApiResponse<MasHospitalResponse>> addHospital(@RequestBody MasHospitalRequest hospitalRequest) {
        ApiResponse<MasHospitalResponse> response = masHospitalService.addHospital(hospitalRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/hospital/updateById/{id}")
    public ResponseEntity<ApiResponse<MasHospitalResponse>> updateHospital(@PathVariable Long id, @RequestBody MasHospitalRequest hospitalRequest) {
        ApiResponse<MasHospitalResponse> response = masHospitalService.updateHospital(id, hospitalRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/hospital/status/{id}")
    public ResponseEntity<ApiResponse<MasHospitalResponse>> changeHospitalStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasHospitalResponse> response = masHospitalService.changeHospitalStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================Mas store Section Controller================================//
    @PostMapping("/storeSection/create")
    public ResponseEntity<ApiResponse<MasStoreSectionResponse>> addMasStoreSection(@RequestBody MasStoreSectionRequest masStoreSectionRequest) {
        ApiResponse<MasStoreSectionResponse> response = masStoreSectionService.addMasStoreSection(masStoreSectionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/storeSection/getAll/{flag}")
    public ApiResponse<List<MasStoreSectionResponse>> getAllStoreSection(@PathVariable int flag) {
        return masStoreSectionService.getAllStoreSection(flag);
    }

    @GetMapping("/storeSection/getById/{id}")
    public ResponseEntity<ApiResponse<MasStoreSectionResponse>> getStoreSectionById(@PathVariable Integer id) {
        ApiResponse<MasStoreSectionResponse> response = masStoreSectionService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/storeSection/status/{id}")
    public ResponseEntity<ApiResponse<MasStoreSectionResponse>> changeMasStoreSectionStatus(@PathVariable int id, @RequestParam String status) {
        ApiResponse<MasStoreSectionResponse> response = masStoreSectionService.changeStoreSectionStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/storeSection/updateById/{id}")
    public ResponseEntity<ApiResponse<MasStoreSectionResponse>> updateMasStoreSection(@PathVariable int id, @RequestBody MasStoreSectionRequest masStoreSectionRequest) {
        ApiResponse<MasStoreSectionResponse> response = masStoreSectionService.updateStoreSection(id, masStoreSectionRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/storeSection/findByItemType/{id}")
    public ApiResponse<List<MasStoreSectionResponse>> findStoreSectionByItemType(@PathVariable int id) {
        return masStoreSectionService.findStoreSectionByItemType(id);
    }




    //    ================================Mas Item Class Controller================================//
    @PostMapping("/masItemClass/create")
    public ResponseEntity<ApiResponse<MasItemClassResponse>> addMasItemClass(@RequestBody MasItemClassRequest masItemClassRequest) {
        ApiResponse<MasItemClassResponse> response = masItemClassService.addMasItemClass(masItemClassRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/masItemClass/getAll/{flag}")
    public ApiResponse<List<MasItemClassResponse>> getAllMasItemClass(@PathVariable int flag) {
        return masItemClassService.getAllMasItemClass(flag);
    }
    @GetMapping("/masItemClass/getAllBySectionId/{id}")
    public ApiResponse<List<MasItemClassResponse>> getBySectionId(@PathVariable int id) {
        return masItemClassService.getAllBySectionId(id);
    }

    @GetMapping("/masItemClass/getById/{id}")
    public ResponseEntity<ApiResponse<MasItemClassResponse>> getItemClassById(@PathVariable Integer id) {
        ApiResponse<MasItemClassResponse> response = masItemClassService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masItemClass/status/{id}")
    public ResponseEntity<ApiResponse<MasItemClassResponse>> changeMasItemClassStatus(@PathVariable int id, @RequestParam String status) {
        ApiResponse<MasItemClassResponse> response = masItemClassService.changeMasItemClassStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masItemClass/updateById/{id}")
    public ResponseEntity<ApiResponse<MasItemClassResponse>> updateMasItemClass(@PathVariable int id, @RequestBody MasItemClassRequest masItemClassdRequest) {
        ApiResponse<MasItemClassResponse> response = masItemClassService.updateMasItemClass(id, masItemClassdRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //    ================================Mas Item Category Controller================================//


    @PostMapping("/masItemCategory/create")
    public ResponseEntity<ApiResponse<MasItemCategoryResponse>> addMasItemCategory(@RequestBody MasItemCategoryRequest masItemCategoryRequest) {
        ApiResponse<MasItemCategoryResponse> response = masItemCategoryService.addMasItemCategory(masItemCategoryRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/masItemCategory/getAll/{flag}")
    public ApiResponse<List<MasItemCategoryResponse>> getAllMasItemCategory(@PathVariable int flag) {
        return masItemCategoryService.getAllMasItemCategory(flag);
    }

    @GetMapping("/masItemCategory/getById/{id}")
    public ResponseEntity<ApiResponse<MasItemCategoryResponse>> getItemCategoryById(@PathVariable Integer id) {
        ApiResponse<MasItemCategoryResponse> response = masItemCategoryService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masItemCategory/status/{id}")
    public ResponseEntity<ApiResponse<MasItemCategoryResponse>> changeMasItemCategoryStatus(@PathVariable int id, @RequestParam String status) {
        ApiResponse<MasItemCategoryResponse> response = masItemCategoryService.changeMasItemCategoryStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masItemCategory/updateById/{id}")
    public ResponseEntity<ApiResponse<MasItemCategoryResponse>> updateMasItemCategory(@PathVariable int id, @RequestBody MasItemCategoryRequest masItemCategoryRequest) {
        ApiResponse<MasItemCategoryResponse> response = masItemCategoryService.updateMasItemClass(id, masItemCategoryRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/masItemCategory/findBySectionId/{id}")
    public ApiResponse<List<MasItemCategoryResponse>> findByMasItemCategoryBbySectionId(@PathVariable int id) {
        return masItemCategoryService.findByMasItemCategoryBbySectionId(id);
    }


    //    ================================Mas Opd Service Controller================================//

    @GetMapping("/masServiceOpd/getByHospitalId/{id}")
    public ResponseEntity<ApiResponse<List<MasServiceOpd>>> getMasServiceOpdByHospitalId(@PathVariable Long id) {
        ApiResponse<List<MasServiceOpd>> response = masServiceOpdService.findByHospitalId(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/masServiceOpd/save")
    public ResponseEntity<ApiResponse<MasServiceOpd>> saveMasServiceOpd(@RequestBody MasServiceOpdRequest request) {
        ApiResponse<MasServiceOpd> response = masServiceOpdService.save(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masServiceOpd/update/{id}")
    public ResponseEntity<ApiResponse<MasServiceOpd>> updateMasServiceOpd(
            @PathVariable Long id,
            @RequestBody MasServiceOpdRequest request) {
        ApiResponse<MasServiceOpd> response = masServiceOpdService.edit(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/masServiceOpd/updateStatus/{id}")
    public ResponseEntity<ApiResponse<MasServiceOpd>> updateMasServiceOpdStatusById(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasServiceOpd> response = masServiceOpdService.updateStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================Mas Service Category Controller================================//


    @GetMapping("/masServiceCategory/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasServiceCategory>>> getAllMasServiceCategory(@PathVariable int flag) {
        ApiResponse<List<MasServiceCategory>> response = masServiceCategoryService.findAll(flag);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/masServiceCategory/save")
    public ResponseEntity<ApiResponse<MasServiceCategory>> saveMasService(@RequestBody MasServiceCategory request) {
        ApiResponse<MasServiceCategory> response = masServiceCategoryService.save(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masServiceCategory/update/{id}")
    public ResponseEntity<ApiResponse<MasServiceCategory>> updateMasService(
            @PathVariable Long id,
            @RequestBody MasServiceCategory request) {
        ApiResponse<MasServiceCategory> response = masServiceCategoryService.edit(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masServiceCategory/updateStatus/{id}")
    public ResponseEntity<ApiResponse<MasServiceCategory>> updateMasServiceStatusById(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasServiceCategory> response = masServiceCategoryService.updateStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/masServiceCategory/getGstConfig/{flag}")
    public ResponseEntity<ApiResponse<GstConfigResponse>> getGstConfig(@PathVariable int flag , @RequestParam(name = "categoryId", required = false) Integer catId) {
        ApiResponse<GstConfigResponse> response = masServiceCategoryService.getGstConfig(flag , catId);

        HttpStatus status = (response.getStatus() == HttpStatus.OK.value()) ? HttpStatus.OK : HttpStatus.NOT_FOUND;

        return new ResponseEntity<>(response, status);
    }


    //    ================================Mas Store Item Controller================================//
    @PostMapping("/masStoreItem/create")
    public ResponseEntity<ApiResponse<MasStoreItemResponse>> addMasItemCategory(@RequestBody MasStoreItemRequest masStoreItemRequest) {
        ApiResponse<MasStoreItemResponse> response = masStoreItemService.addMasStoreItem(masStoreItemRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/masStoreItem/getById/{id}")
    public ResponseEntity<ApiResponse<MasStoreItemResponse>> getMasStoreItemById(@PathVariable Long id) {
        ApiResponse<MasStoreItemResponse> response = masStoreItemService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/masStoreItem/getAll/{flag}")
    public ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItem(@PathVariable int flag) {
        return masStoreItemService.getAllMasStoreItem(flag);
    }

    @GetMapping("/masStoreItemWithotStock/getAll/{flag}")
    public ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItemWithotStock(@PathVariable int flag) {
        return masStoreItemService.getAllMasStoreItemWithotStock(flag);
    }

    @PutMapping("/masStoreItem/update/{id}")
    public ResponseEntity<ApiResponse<MasStoreItemResponse>> updateMasStoreItem(
            @PathVariable Long id,
            @RequestBody MasStoreItemRequest request) {
        ApiResponse<MasStoreItemResponse> response = masStoreItemService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masStoreItem/status/{id}")
    public ResponseEntity<ApiResponse<MasStoreItemResponse>> changeMasStoreItemStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasStoreItemResponse> response = masStoreItemService.changeMasStoreItemStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/masStoreItem/getByCode/{code}")
    public ResponseEntity<ApiResponse<MasStoreItemResponse>> getMasStoreItemById(@PathVariable String code) {
        ApiResponse<MasStoreItemResponse> response = masStoreItemService.findByCode(code);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/masStoreItem/getAll2/{flag}")
    public ApiResponse<List<MasStoreItemResponse2>> getAllMasStore(@PathVariable int flag) {
        return masStoreItemService.getAllMasStore(flag);
    }

    @GetMapping("/masStoreItem/getAllBySectionOnly/{flag}")
    public ApiResponse<List<MasStoreItemResponseWithStock>> getAllMasStoreItemBySectionOnly(@PathVariable int flag) {
        return masStoreItemService.getAllMasStoreItemBySectionOnly(flag);
    }

    @GetMapping("/masStoreItem/getAllBySectionOnlyDynamic")
    public ApiResponse<Page<MasStoreItemResponseWithStock>> getMasStoreItemDynamic(
            @RequestParam int flag,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return masStoreItemService.getMasStoreItemDynamic(flag, search, page, size);
    }



    //    ================================Mas HSN  Controller================================//

    @GetMapping("/masHSN/getAll/{flag}")
    public ApiResponse<List<MasHsnResponse>> getAllMasHsn(@PathVariable int flag) {
        return masHsnService.getAllMasStoreItem(flag);
    }

    @GetMapping("/masHSN/getById/{id}")
    public ResponseEntity<ApiResponse<MasHsnResponse>> getMasHSNById(@PathVariable String id) {
        ApiResponse<MasHsnResponse> response = masHsnService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/masHSN/create")
    public ResponseEntity<ApiResponse<MasHsnResponse>> addMasHSN(@RequestBody MasHsnRequest masHsnRequest) {
        ApiResponse<MasHsnResponse> response = masHsnService.addMasHSN(masHsnRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/masHSN/update/{id}")
    public ResponseEntity<ApiResponse<MasHsnResponse>> updateMasHSN(
            @PathVariable String id,
            @RequestBody MasHsnRequest request) {
        ApiResponse<MasHsnResponse> response = masHsnService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masHSN/status/{id}")
    public ResponseEntity<ApiResponse<MasHsnResponse>> changeMasHsnStatus(@PathVariable String id, @RequestParam String status) {
        ApiResponse<MasHsnResponse> response = masHsnService.changeMasHsnStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================Mas Brand  Controller================================//

    @GetMapping("/masBrand/getAll/{flag}")
    public ApiResponse<List<MasBrandResponse>> getAllMasBrand(@PathVariable int flag) {
        return masBrandService.getAllMasBrand(flag);
    }

    @GetMapping("/masBrand/getById/{id}")
    public ResponseEntity<ApiResponse<MasBrandResponse>> getMasBrandById(@PathVariable Long id) {
        ApiResponse<MasBrandResponse> response = masBrandService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/masBrand/create")
    public ResponseEntity<ApiResponse<MasBrandResponse>> addMasBrand(@RequestBody MasBrandRequest masBrandRequest) {
        ApiResponse<MasBrandResponse> response = masBrandService.addMasBrand(masBrandRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/masBrand/update/{id}")
    public ResponseEntity<ApiResponse<MasBrandResponse>> updateMasBrand(
            @PathVariable Long id,
            @RequestBody MasBrandRequest request) {
        ApiResponse<MasBrandResponse> response = masBrandService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masBrand/status/{id}")
    public ResponseEntity<ApiResponse<MasBrandResponse>> changeMasBrandStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasBrandResponse> response = masBrandService.changeMasBrandStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    //    ================================Mas Manufacturer  Controller================================//

    @GetMapping("/masManufacturer/getAll/{flag}")
    public ApiResponse<List<MasManufacturer>> getAllMasManufacturer(@PathVariable int flag) {
        return masManufacturerService.getAllMasManufacturer(flag);
    }

    @GetMapping("/masManufacturer/getById/{id}")
    public ResponseEntity<ApiResponse<MasManufacturer>> getMasManufacturer(@PathVariable Long id) {
        ApiResponse<MasManufacturer> response = masManufacturerService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/masManufacturer/create")
    public ResponseEntity<ApiResponse<MasManufacturer>> addMasManufacturer(@RequestBody MasManufacturerRequest masManufacturerRequest) {
        ApiResponse<MasManufacturer> response = masManufacturerService.addMasManufacturer(masManufacturerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

        @PutMapping("/masManufacturer/update/{id}")
    public ResponseEntity<ApiResponse<MasManufacturer>> updateMasManufacturer(
            @PathVariable Long id,
            @RequestBody MasManufacturerRequest request) {
        ApiResponse<MasManufacturer> response = masManufacturerService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/masManufacturer/status/{id}")
    public ResponseEntity<ApiResponse<MasManufacturer>> changeMasManufacturer(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasManufacturer> response = masManufacturerService.changeMasManufacturer(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);


    }
    //    ================================ DgMasCollection Controller================================//


    @GetMapping("/DgMasCollection/getAll/{flag}")
    public ApiResponse<List<DgMasCollectionResponse>> getAllDgMasCollection(@PathVariable int flag) {
        return dgMasCollectionService.getDgMasCollection(flag);
  }

   @GetMapping("/DgMasCollection/getById/{id}")
    public ResponseEntity<ApiResponse<DgMasCollectionResponse>> getDgMasCollectionById(@PathVariable Long id) {
       ApiResponse<DgMasCollectionResponse> response = dgMasCollectionService.findById(id);
       return new ResponseEntity<>(response, HttpStatus.OK);
   }

    @PostMapping("/DgMasCollection/create")
    public ResponseEntity<ApiResponse<DgMasCollectionResponse>> addDgMasCollection(@RequestBody DgMasCollectionRequest dgMasCollectionRequest) {
        ApiResponse<DgMasCollectionResponse> response =  dgMasCollectionService.addDgMasCollection(dgMasCollectionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/DgMasCollection/update/{id}")
    public ResponseEntity<ApiResponse<DgMasCollectionResponse>> updateDgMasCollection(
            @PathVariable Long id,
            @RequestBody DgMasCollectionRequest request) {
        ApiResponse<DgMasCollectionResponse> response = dgMasCollectionService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/DgMasCollection/status/{id}")
    public ResponseEntity<ApiResponse<DgMasCollectionResponse>> changeDgMasCollectionStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<DgMasCollectionResponse> response = dgMasCollectionService.changeDgMasCollectionStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ================================ DgFixedValue Controller================================//


    @GetMapping("/DgFixedValue/getAll")
    public ApiResponse<List<DgFixedValue>> getAllDgFixedValue() {
        return dgFixedValueService.getDgFixedValue();
    }


    //    ================================ MasSymptoms Controller================================//

    @PostMapping ("/MasSymptoms/create")
    public ResponseEntity<ApiResponse<MasSymptomsResponse>> addSymptom(@RequestBody MasSymptomsRequest symptomsReq){
        ApiResponse<MasSymptomsResponse> response = masSymptomsService.createSymptom(symptomsReq);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/MasSymptoms/update/{id}")
    public ResponseEntity<ApiResponse<MasSymptomsResponse>> updateSymptom(
            @PathVariable Long id,
            @RequestBody MasSymptomsRequest symptomsReq){
        ApiResponse<MasSymptomsResponse> response = masSymptomsService.updateSymptom(id, symptomsReq);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/MasSymptoms/changeStatus/{id}")
    public ResponseEntity<ApiResponse<MasSymptomsResponse>> changeSymptomStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasSymptomsResponse> response = masSymptomsService.changeSymptomStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/MasSymptoms/getSymptomsById/{id}")
    public ResponseEntity<ApiResponse<MasSymptomsResponse>> getSymptomsById(
            @PathVariable Long id ) {
        ApiResponse<MasSymptomsResponse> response = masSymptomsService.findBySymptomId(id);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/MasSymptoms/getAllSymptoms/{flag}")
    public  ApiResponse<List<MasSymptomsResponse>> getAllSymptomsByFlag(
            @PathVariable int flag
    ) {
        return masSymptomsService.getAllSymptoms(flag);
    }

  //  ============================================ Mas Investigtion Category=====================================

    @PostMapping("/masInvestigationCategory/create")
    public ResponseEntity<ApiResponse<String>> createCategory(@RequestBody MasInvestigationCategoryRequest request){
        return new ResponseEntity<>(masInvestigationCategoryService.create(request),HttpStatus.CREATED);
    }

    @PutMapping("/masInvestigationCategory/update/{id}")
    public ResponseEntity<ApiResponse<String>> updateMasInvestigationCategory(@PathVariable Long id, @RequestBody MasInvestigationCategoryRequest  request) {
        return new ResponseEntity<>(masInvestigationCategoryService.update(id,request), HttpStatus.OK);
    }
    @GetMapping("/masInvestigationCategory/findAll")
    public ResponseEntity<ApiResponse<List<MasInvestigationCategoryResponse>>> getCategory() {
        ApiResponse<List<MasInvestigationCategoryResponse>> response = masInvestigationCategoryService.get();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/masInvestigationCategory/getById/{id}")
    public ResponseEntity<ApiResponse<MasInvestigationCategoryResponse>> getByCategory(@PathVariable Long id) {
        ApiResponse<MasInvestigationCategoryResponse> response = masInvestigationCategoryService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    //  ============================================ Mas Investigtion Methodology=====================================
    @PostMapping("/masInvestigationMethodology/create")
    public ResponseEntity<ApiResponse<String>> createMethodology(@RequestBody MasInvestigationMethodologyRequest request){
        return new ResponseEntity<>(masInvestigationMethodologyService.create(request),HttpStatus.CREATED);
    }
    @PutMapping("/masInvestigationMethodology/update/{id}")
    public ResponseEntity<ApiResponse<String>> updateMasInvestigationMethodology(@PathVariable Long id, @RequestBody MasInvestigationMethodologyRequest  request) {
        return new ResponseEntity<>(masInvestigationMethodologyService.update(id,request), HttpStatus.OK);
    }
    @GetMapping("/masInvestigationMethodology/findAll")
    public ResponseEntity<ApiResponse<List<MasInvestigationMethodologyResponse >>> getMethodology() {
        ApiResponse<List<MasInvestigationMethodologyResponse >> response = masInvestigationMethodologyService.get();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/masInvestigationMethodology/getById/{id}")
    public ResponseEntity<ApiResponse<MasInvestigationMethodologyResponse >> getMasHSNById(@PathVariable Long id) {
        ApiResponse<MasInvestigationMethodologyResponse > response = masInvestigationMethodologyService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



//    ===============================Mas Icd=====================================
//    @GetMapping("/masIcd/all/{flag}")
//    public ApiResponse<List<MasIcdResponse>> getAllIcds(@PathVariable int flag) {
//        return masIcdService.getAllIcds(flag);
//    }

    @GetMapping("/masIcd/all")
    public ApiResponse<Page<MasIcdResponse>> getAllIcd(
            @RequestParam int flag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        return masIcdService.getAllIcd(flag, page, size, search);
    }


    //  ============================================ Mas Care Level=====================================
    @PostMapping("/mas-care-level/create")
    public ResponseEntity<?> createCareLevel(@RequestBody MasCareLevelRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(masCareLevelService.createCareLevel(request));
    }
    @PutMapping("/mas-care-level/update/{careId}")
    public ResponseEntity<?> updateCareLevel(@PathVariable Long careId, @RequestBody MasCareLevelRequest  request) {
        return ResponseEntity.ok(masCareLevelService.updateCareLevel(careId,request));
    }
    @PutMapping("/mas-care-level/update-status/{careId}")
    public ResponseEntity<?> updateStatusCareLevel(@PathVariable Long careId, @RequestParam String status) {
        return ResponseEntity.ok(masCareLevelService.changeActiveStatus(careId,status));
    }
    @GetMapping("/mas-care-level/getAll/{flag}")
    public ResponseEntity<?> getAllCareLevel(@PathVariable int flag){
        return ResponseEntity.ok(masCareLevelService.getAll(flag));
    }
    @GetMapping("/mas-care-level/getById/{careId}")
    public ResponseEntity<?> getMasCareById(@PathVariable Long careId) {
        return ResponseEntity.ok(masCareLevelService.getById(careId));
    }


    //    ===============================Mas Ward Category=====================================

    @GetMapping("/masWardCategory/getAll/{flag}")
    public ApiResponse<List<MasWardCategoryResponse >> getAllMasWard(@PathVariable int flag) {
        return masWardCategoryService.getAllMasWardCategory(flag);
    }

    @GetMapping("/masWardCategory/getById/{id}")
    public ResponseEntity<ApiResponse<MasWardCategoryResponse >> getMasWardById(@PathVariable Long id) {
        ApiResponse<MasWardCategoryResponse> response = masWardCategoryService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/masWardCategory/create")
    public ResponseEntity<ApiResponse<MasWardCategoryResponse >> addMasWard(@RequestBody MasWardCategoryRequest  request) {
        ApiResponse<MasWardCategoryResponse> response = masWardCategoryService.addMasWard(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/masWardCategory/update/{id}")
    public ResponseEntity<ApiResponse<MasWardCategoryResponse >> updateMasWard(
            @PathVariable Long id,
            @RequestBody MasWardCategoryRequest  request) {
        ApiResponse<MasWardCategoryResponse> response = masWardCategoryService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masWardCategory/status/{id}")
    public ResponseEntity<ApiResponse<MasWardCategoryResponse >> changeMasWardStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasWardCategoryResponse> response = masWardCategoryService.changeMasWardStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    ===============================Mas Ward =====================================

    @GetMapping("/masWard/getAll/{flag}")
    public ApiResponse<List<MasWardResponse >> getMasWard(@PathVariable int flag) {
        return masWardService.getAllMasWardCategory(flag);
    }

    @GetMapping("/masWard/getById/{id}")
    public ResponseEntity<ApiResponse<MasWardResponse >> getWardById(@PathVariable Long id) {
        ApiResponse<MasWardResponse> response = masWardService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/masWard/create")
    public ResponseEntity<ApiResponse<MasWardResponse >> addWard(@RequestBody MasWardRequest  request) {
        ApiResponse<MasWardResponse> response = masWardService.addMasWard(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/masWard/update/{id}")
    public ResponseEntity<ApiResponse<MasWardResponse >> updateWard(
            @PathVariable Long id,
            @RequestBody MasWardRequest  request) {
        ApiResponse<MasWardResponse> response = masWardService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masWard/status/{id}")
    public ResponseEntity<ApiResponse<MasWardResponse >> changeWardStatus(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasWardResponse> response = masWardService.changeMasWardStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //  ============================================ Mas Room Category =====================================


    @PostMapping("mas-room-category/create")
    public ResponseEntity<?> createRoomCategory(@RequestBody MasRoomCategoryRequest request) {
        return ResponseEntity.ok(masRoomCategoryService.createRoomCategory(request));
    }

    @PutMapping("mas-room-category/update/{roomCategoryId}")
    public ResponseEntity<?> updateRoomCategory(
            @PathVariable Long roomCategoryId,
            @RequestBody MasRoomCategoryRequest request) {
        return ResponseEntity.ok(masRoomCategoryService.updateRoomCategory(roomCategoryId, request));
    }

    @PutMapping("mas-room-category/status/{roomCategoryId}")
    public ResponseEntity<ApiResponse<MasRoomCategoryResponse>> changeStatus(
            @PathVariable Long roomCategoryId,
            @RequestParam String status) {
        return ResponseEntity.ok(masRoomCategoryService.changeActiveStatus(roomCategoryId, status));
    }

    @GetMapping("mas-room-category/getById/{roomCategoryId}")
    public ResponseEntity<?> getById(@PathVariable Long roomCategoryId) {
        return ResponseEntity.ok(masRoomCategoryService.getById(roomCategoryId));
    }

    @GetMapping("mas-room-category/getAll/{flag}")
    public ResponseEntity<?> getAllRoomCategory(@PathVariable int flag) {
        return ResponseEntity.ok(masRoomCategoryService.getAll(flag));
    }


    //  ============================================ Mas Room =====================================


    @PostMapping("mas-room/create")
    public ResponseEntity<ApiResponse<MasRoomResponse>> create(@RequestBody MasRoomRequest request) {
        return ResponseEntity.ok(masRoomService.createRoom(request));
    }

    @PutMapping("mas-room/update/{roomId}")
    public ResponseEntity<ApiResponse<MasRoomResponse>> update(
            @PathVariable Long roomId,
            @RequestBody MasRoomRequest request) {
        return ResponseEntity.ok(masRoomService.updateRoom(roomId, request));
    }

    @PutMapping("mas-room/status/{roomId}")
    public ResponseEntity<ApiResponse<MasRoomResponse>> changeRoomStatus(
            @PathVariable Long roomId,
            @RequestParam String status) {
        return ResponseEntity.ok(masRoomService.changeActiveStatus(roomId, status));
    }

    @GetMapping("mas-room/{roomId}")
    public ResponseEntity<ApiResponse<MasRoomResponse>> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(masRoomService.getById(roomId));
    }

    @GetMapping("mas-room/all/{flag}")
    public ResponseEntity<ApiResponse<List<MasRoomResponse>>> getAllRoom(@PathVariable int flag) {
        return ResponseEntity.ok(masRoomService.getAll(flag));
    }



    //  ============================================ Mas Bed Type =====================================


    @PostMapping("masBedType/create")
    public ResponseEntity<?> createBed(@RequestBody MasBedTypeRequest request) {
        return ResponseEntity.ok(masBedTypeService.masBedTypeCreate(request));
    }

    @PutMapping("masBedType/update/{id}")
    public ResponseEntity<?> updateBed(
            @PathVariable Long id,
            @RequestBody MasBedTypeRequest request) {
        return ResponseEntity.ok(masBedTypeService.masBedTypeUpdate(id, request));
    }

    @PutMapping("masBedType/status/{id}")
    public ResponseEntity<ApiResponse<MasBedTypeResponse>> changeBedStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBedTypeService.changeActiveStatus(id, status));
    }

    @GetMapping("masBedType/getById/{id}")
    public ResponseEntity<?> getByIdBed(@PathVariable Long id) {
        return ResponseEntity.ok(masBedTypeService.getById(id));
    }

    @GetMapping("masBedType/getAll/{flag}")
    public ResponseEntity<?> getAllBed(@PathVariable int flag) {
        return ResponseEntity.ok(masBedTypeService.getAll(flag));
    }

    //  ============================================ Mas Bed Status =====================================


    @PostMapping("mas-bed-status/create")
    public ResponseEntity<ApiResponse<MasBedStatusResponse>> create(@RequestBody MasBedStatusRequest request) {
        return ResponseEntity.ok(masBedStatusService.createBedStatus(request));
    }

    @PutMapping("mas-bed-status/update/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody MasBedStatusRequest request) {
        return ResponseEntity.ok(masBedStatusService.updateBedStatus(id, request));
    }

    @PutMapping("mas-bed-status/status/{id}")
    public ResponseEntity<ApiResponse<MasBedStatusResponse>> changeMasBedStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBedStatusService.changeActiveStatus(id, status));
    }

    @GetMapping("mas-bed-status/getById/{id}")
    public ResponseEntity<ApiResponse<MasBedStatusResponse>> getMasBedStatusById(@PathVariable Long id) {
        return ResponseEntity.ok(masBedStatusService.getById(id));
    }

    @GetMapping("mas-bed-status/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBedStatusResponse>>> getAllBedStatus(@PathVariable int flag) {
        return ResponseEntity.ok(masBedStatusService.getAll(flag));
    }


    //  ============================================ Mas Bed  =====================================


    @PostMapping("masBed/create")
    public ResponseEntity<?> createMasBed(@RequestBody MasBedRequest request) {
        return ResponseEntity.ok(masBedService.createRoomCategory(request));
    }

    @PutMapping("masBed/update/{id}")
    public ResponseEntity<?> updateMasBed(
            @PathVariable Long id,
            @RequestBody MasBedRequest request) {
        return ResponseEntity.ok(masBedService.updateRoomCategory(id, request));
    }

    @PutMapping("masBed/status/{id}")
    public ResponseEntity<ApiResponse<MasBedResponse>> changeBedStatuss(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBedService.changeActiveStatus(id, status));
    }

    @GetMapping("masBed/getById/{id}")
    public ResponseEntity<?> getByIdMasBed(@PathVariable Long id) {
        return ResponseEntity.ok(masBedService.getById(id));
    }

    @GetMapping("masBed/getAll/{flag}")
    public ResponseEntity<?> getAllMasBed(@PathVariable int flag) {
        return ResponseEntity.ok(masBedService.getAll(flag));
    }


    //    =============================== Mas Medical History =====================================

    @GetMapping("/masMedicalHistory/getAll/{flag}")
    public ResponseEntity<?> getMasMedicalHistory(@PathVariable int flag) {
        return ResponseEntity.ok(masMedicalHistoryService.getAllMas(flag));
    }

    @PostMapping("/masMedicalHistory/create")
    public ResponseEntity<ApiResponse<MasMedicalHistoryResponse>> addMasMedicalHistory(@RequestBody  MasMedicalHistoryRequest request) {
        ApiResponse<MasMedicalHistoryResponse> response = masMedicalHistoryService.addMas(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/masMedicalHistory/update/{id}")
    public ResponseEntity<ApiResponse<MasMedicalHistoryResponse>> updateMasMedicalHistory(
            @PathVariable Long id,
            @RequestBody  MasMedicalHistoryRequest  request) {
        ApiResponse<MasMedicalHistoryResponse> response = masMedicalHistoryService.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/masMedicalHistory/status/{id}")
    public ResponseEntity<ApiResponse<MasMedicalHistoryResponse>> changeMasMedicalHistory(@PathVariable Long id, @RequestParam String status) {
        ApiResponse<MasMedicalHistoryResponse> response = masMedicalHistoryService.changeMasStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //    =============================== Mas Treatment Advice =====================================


    @GetMapping("masTreatmentAdvise/getAll/{flag}")
    public ResponseEntity<?> getAllTreatment(@PathVariable int flag) {
        return ResponseEntity.ok(service.getAll(flag));
    }

    @PostMapping("masTreatmentAdvise/create")
    public ResponseEntity<ApiResponse<MasTreatmentAdviseResponse>> add(
            @RequestBody MasTreatmentAdviseRequest request) {

        ApiResponse<MasTreatmentAdviseResponse> response = service.add(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("masTreatmentAdvise/update/{id}")
    public ResponseEntity<ApiResponse<MasTreatmentAdviseResponse>> update(
            @PathVariable Long id,
            @RequestBody MasTreatmentAdviseRequest request) {

        ApiResponse<MasTreatmentAdviseResponse> response = service.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("masTreatmentAdvise/status/{id}")
    public ResponseEntity<ApiResponse<MasTreatmentAdviseResponse>> changeStatusTreatment(
            @PathVariable Long id,
            @RequestParam String status) {

        ApiResponse<MasTreatmentAdviseResponse> response = service.changeStatus(id, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    //    ===============================Mas Procedure Type =====================================

    @GetMapping("masProcedureType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasProcedureTypeResponse>>> getAllProcedure(@PathVariable int flag) {
        return  ResponseEntity.ok(masProcedureTypeService.getAllProcedureType(flag));
    }

    @GetMapping("masProcedureType/getById/{id}")
    public ResponseEntity<ApiResponse<MasProcedureTypeResponse>> getByIdProcedureType(@PathVariable Long id) {
        return ResponseEntity.ok( masProcedureTypeService.findById(id));
    }

    @PostMapping("masProcedureType/create")
    public ResponseEntity<ApiResponse<MasProcedureTypeResponse>> createProcedure(@RequestBody MasProcedureTypeRequest request) {
        return new ResponseEntity<>( masProcedureTypeService.addProcedureType(request), HttpStatus.CREATED);
    }

    @PutMapping("masProcedureType/update/{id}")
    public ResponseEntity<ApiResponse<MasProcedureTypeResponse>> updateProcedure(
            @PathVariable Long id, @RequestBody MasProcedureTypeRequest request) {

        return ResponseEntity.ok( masProcedureTypeService.update(id, request));
    }

    @PutMapping("masProcedureType/status/{id}")
    public ResponseEntity<ApiResponse<MasProcedureTypeResponse>> changeStatusProcedureType(
            @PathVariable Long id, @RequestParam String status) {

        return ResponseEntity.ok( masProcedureTypeService.changeStatus(id, status));
    }

    //    ===============================Mas Procedure  =====================================

    @GetMapping("masProcedure/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasProcedureResponse>>> getAllMasProcedure(@PathVariable int flag) {

        log.info("Get all MasProcedure, flag={}", flag);
        return ResponseEntity.ok(masProcedureService.getAllMasProcedure(flag));
    }

    @GetMapping("masProcedureFilter/getAll")
    public ApiResponse<Page<MasProcedureResponse>> getAllProceduresWIthFilter(
            @RequestParam int flag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        return masProcedureService.getAllProceduresWIthFilter(flag, page, size, search);
    }




    @GetMapping("masProcedure/getById/{id}")
    public ResponseEntity<ApiResponse<MasProcedureResponse>> getByIdProcedure(@PathVariable Long id) {

        log.info("Get MasProcedure By Id={}", id);
        return ResponseEntity.ok(masProcedureService.getMasProcedureById(id));
    }


    @PostMapping("masProcedure/create")
    public ResponseEntity<ApiResponse<MasProcedureResponse>> createProcedure(
            @RequestBody MasProcedureRequest request) {

        log.info("Create MasProcedure request={}", request);
        return new ResponseEntity<>(masProcedureService.addMasProcedure(request), HttpStatus.CREATED);
    }


    @PutMapping("masProcedure/update/{id}")
    public ResponseEntity<ApiResponse<MasProcedureResponse>> updateProcedure(
            @PathVariable Long id,
            @RequestBody MasProcedureRequest request) {

        log.info("Update MasProcedure id={}, request={}", id, request);
        return ResponseEntity.ok(masProcedureService.updateMasProcedure(id, request));
    }


    @PutMapping("masProcedure/status/{id}")
    public ResponseEntity<ApiResponse<MasProcedureResponse>> changeStatusProcedure(
            @PathVariable Long id,
            @RequestParam String status) {

        log.info("Change status MasProcedure id={}, status={}", id, status);
        return ResponseEntity.ok(masProcedureService.changeStatus(id, status));
    }
    //    ===============================Mas Meal Type  =====================================

    @GetMapping("masMealType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasMealTypeResponse>>> getAllMealType(@PathVariable int flag) {
        return ResponseEntity.ok(masMealTypeService.getAllMealType(flag)) ;
    }

    @GetMapping("masMealType/getById/{id}")
    public ResponseEntity<ApiResponse<MasMealTypeResponse>> getByIdMealType(@PathVariable Long id) {
        return ResponseEntity.ok(masMealTypeService.findById(id));
    }

    @PostMapping("masMealType/create")
    public ResponseEntity<ApiResponse<MasMealTypeResponse>> createMealType(@RequestBody MasMealTypeRequest request) {
        return new ResponseEntity<>(masMealTypeService.addMealType(request), HttpStatus.CREATED);
    }

    @PutMapping("masMealType/update/{id}")
    public ResponseEntity<ApiResponse<MasMealTypeResponse>> updateMealType(
            @PathVariable Long id,
            @RequestBody MasMealTypeRequest request) {
        return ResponseEntity.ok(masMealTypeService.update(id, request));
    }

    @PutMapping("masMealType/status/{id}")
    public ResponseEntity<ApiResponse<MasMealTypeResponse>> changeStatusMealType(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masMealTypeService.changeStatus(id, status));
    }

    //    ===============================Mas Diet Type  =====================================

    @GetMapping("masDietType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasDietTypeResponse>>> getAllDietType(@PathVariable int flag) {
        return ResponseEntity.ok(masDietTypeService.getAllDietType(flag));
    }

    @GetMapping("masDietType/getById/{id}")
    public ResponseEntity<ApiResponse<MasDietTypeResponse>> getByIdDietType(@PathVariable Long id) {
        return ResponseEntity.ok( masDietTypeService.findById(id));
    }

    @PostMapping("masDietType/create")
    public ResponseEntity<ApiResponse<MasDietTypeResponse>> createDietType(@RequestBody MasDietTypeRequest request) {
        return new ResponseEntity<>( masDietTypeService.addDietType(request), HttpStatus.CREATED);
    }

    @PutMapping("masDietType/update/{id}")
    public ResponseEntity<ApiResponse<MasDietTypeResponse>> updateDietType(
            @PathVariable Long id,
            @RequestBody MasDietTypeRequest request) {
        return ResponseEntity.ok( masDietTypeService.update(id, request));
    }

    @PutMapping("masDietType/status/{id}")
    public ResponseEntity<ApiResponse<MasDietTypeResponse>> changeStatusDietType(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok( masDietTypeService.changeStatus(id, status));

    }
    //    ===============================Mas Diet Preference =====================================

    @GetMapping("masDietPreference/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasDietPreferenceResponse>>> getAllDietPreference(@PathVariable int flag) {
        return ResponseEntity.ok(masDietPreferenceService.getAll(flag));
    }

    @GetMapping("masDietPreference/getById/{id}")
    public ResponseEntity<ApiResponse<MasDietPreferenceResponse>> getByIdDietPreference(@PathVariable Long id) {
        ApiResponse<MasDietPreferenceResponse> response = masDietPreferenceService.getById(id);


        return ResponseEntity.ok(response);
    }

    @PostMapping("masDietPreference/create")
    public ResponseEntity<ApiResponse<MasDietPreferenceResponse>> createDietPreference(
            @RequestBody MasDietPreferenceRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(masDietPreferenceService.create(request));
    }

    @PutMapping("masDietPreference/update/{id}")
    public ResponseEntity<ApiResponse<MasDietPreferenceResponse>> updateDietPreference(
            @PathVariable Long id,
            @RequestBody MasDietPreferenceRequest request) {

        ApiResponse<MasDietPreferenceResponse> response = masDietPreferenceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("masDietPreference/status/{id}")
    public ResponseEntity<ApiResponse<MasDietPreferenceResponse>> changeStatusDietPreference(
            @PathVariable Long id,
            @RequestParam String status) {
        ApiResponse<MasDietPreferenceResponse> response = masDietPreferenceService.changeStatus(id, status);
        return ResponseEntity.ok(response);
    }

    //    ===============================Mas Diet Schedule Status=====================================

    @GetMapping("masDietSchedule/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasDietScheduleStatusResponse>>> getAllSchedule(@PathVariable int flag) {
        return ResponseEntity.ok(masDietScheduleStatusService.getAll(flag));
    }

    @GetMapping("masDietSchedule/getById/{id}")
    public ResponseEntity<ApiResponse<MasDietScheduleStatusResponse>> getByIdSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(masDietScheduleStatusService.getById(id));
    }

    @PostMapping("masDietSchedule/create")
    public ResponseEntity<ApiResponse<MasDietScheduleStatusResponse>> createSchedule(
            @RequestBody MasDietScheduleStatusRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(masDietScheduleStatusService.create(request));
    }

    @PutMapping("masDietSchedule/update/{id}")
    public ResponseEntity<ApiResponse<MasDietScheduleStatusResponse>> updateSchedule(
            @PathVariable Long id,
            @RequestBody MasDietScheduleStatusRequest request) {

        return ResponseEntity.ok(masDietScheduleStatusService.update(id, request));
    }

    @PutMapping("masDietSchedule/status/{id}")
    public ResponseEntity<ApiResponse<MasDietScheduleStatusResponse>> changeStatusSchedule(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(masDietScheduleStatusService.changeStatus(id, status));
    }

    //    ===============================Mas Admission Type=====================================

    @GetMapping("masAdmissionType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasAdmissionTypeResponse>>> getAllAdmission (@PathVariable int flag) {
        return ResponseEntity.ok(masAdmissionTypeService.getAll(flag));
    }

    @GetMapping("masAdmissionType/getById/{id}")
    public ResponseEntity<ApiResponse<MasAdmissionTypeResponse>> getByIdAdmission (@PathVariable Long id) {
        return ResponseEntity.ok(masAdmissionTypeService.getById(id));
    }

    @PostMapping("masAdmissionType/create")
    public ResponseEntity<ApiResponse<MasAdmissionTypeResponse>> createAdmission (
            @RequestBody MasAdmissionTypeRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(masAdmissionTypeService.create(request));
    }

    @PutMapping("masAdmissionType/update/{id}")
    public ResponseEntity<ApiResponse<MasAdmissionTypeResponse>> updateAdmission (
            @PathVariable Long id,
            @RequestBody MasAdmissionTypeRequest request) {

        return ResponseEntity.ok(masAdmissionTypeService.update(id, request));
    }

    @PutMapping("masAdmissionType/status/{id}")
    public ResponseEntity<ApiResponse<MasAdmissionTypeResponse>> changeStatusAdmission (
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(masAdmissionTypeService.changeStatus(id, status));
    }

    //    ===============================Mas Route=====================================

    @GetMapping("masRoute/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasRouteResponse>>> getAllRoute(@PathVariable int flag) {
        return ResponseEntity.ok(masRouteService.getAll(flag));
    }

    @GetMapping("masRoute/getById/{id}")
    public ResponseEntity<ApiResponse<MasRouteResponse>> getByIdRoute(@PathVariable Long id) {
        return ResponseEntity.ok(masRouteService.getById(id));
    }

    @PostMapping("masRoute/create")
    public ResponseEntity<ApiResponse<MasRouteResponse>> createRoute(@RequestBody MasRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(masRouteService.create(request));
    }

    @PutMapping("masRoute/update/{id}")
    public ResponseEntity<ApiResponse<MasRouteResponse>> updateRoute(
            @PathVariable Long id,
            @RequestBody MasRouteRequest request) {
        return ResponseEntity.ok(masRouteService.update(id, request));
    }

    @PutMapping("masRoute/status/{id}")
    public ResponseEntity<ApiResponse<MasRouteResponse>> changeStatusRoute(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masRouteService.changeStatus(id, status));
    }

//    ===============================Mas Intake Type=====================================



    @GetMapping("masIntakeType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasIntakeTypeResponse>>> getAllIntake(@PathVariable int flag) {
        return ResponseEntity.ok( masIntakeTypeService.getAll(flag));
    }

    @GetMapping("masIntakeType/getById/{id}")
    public ResponseEntity<ApiResponse<MasIntakeTypeResponse>> getByIdIntake(@PathVariable Long id) {
        return ResponseEntity.ok( masIntakeTypeService.getById(id));
    }

    @PostMapping("masIntakeType/create")
    public ResponseEntity<ApiResponse<MasIntakeTypeResponse>> createIntake(@RequestBody MasIntakeTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body( masIntakeTypeService.create(request));
    }

    @PutMapping("masIntakeType/update/{id}")
    public ResponseEntity<ApiResponse<MasIntakeTypeResponse>> updateIntake(
            @PathVariable Long id,
            @RequestBody MasIntakeTypeRequest request) {
        return ResponseEntity.ok( masIntakeTypeService.update(id, request));
    }

    @PutMapping("masIntakeType/status/{id}")
    public ResponseEntity<ApiResponse<MasIntakeTypeResponse>> changeStatusIntake(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok( masIntakeTypeService.changeStatus(id, status));
    }

//    ===============================Mas Intake Item=====================================

    @GetMapping("masIntakeItem/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasIntakeItemResponse>>> getAllIntakeItem(@PathVariable int flag) {
        return ResponseEntity.ok(masIntakeItemService.getAll(flag));
    }

    @GetMapping("masIntakeItem/getById/{id}")
    public ResponseEntity<ApiResponse<MasIntakeItemResponse>> getByIdIntakeItem(@PathVariable Long id) {
        return ResponseEntity.ok(masIntakeItemService.getById(id));
    }

    @PostMapping("masIntakeItem/create")
    public ResponseEntity<ApiResponse<MasIntakeItemResponse>> createIntakeItem(@RequestBody MasIntakeItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(masIntakeItemService.create(request));
    }

    @PutMapping("masIntakeItem/update/{id}")
    public ResponseEntity<ApiResponse<MasIntakeItemResponse>> updateIntakeItem(
            @PathVariable Long id,
            @RequestBody MasIntakeItemRequest request) {
        return ResponseEntity.ok(masIntakeItemService.update(id, request));
    }

    @PutMapping("masIntakeItem/status/{id}")
    public ResponseEntity<ApiResponse<MasIntakeItemResponse>> changeStatusIntakeItem(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masIntakeItemService.changeStatus(id, status));
    }

//    ===============================Mas Output Type=====================================

    @GetMapping("masOutputType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasOutputTypeResponse>>> getAllOutputType(@PathVariable int flag) {
        return ResponseEntity.ok( masOutputTypeService.getAll(flag));
    }

    @GetMapping("masOutputType/getById/{id}")
    public ResponseEntity<ApiResponse<MasOutputTypeResponse>> getByIdOutputType(@PathVariable Long id) {
        return ResponseEntity.ok( masOutputTypeService.getById(id));
    }

    @PostMapping("masOutputType/create")
    public ResponseEntity<ApiResponse<MasOutputTypeResponse>> createOutputType(@RequestBody MasOutputTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body( masOutputTypeService.create(request));
    }

    @PutMapping("masOutputType/update/{id}")
    public ResponseEntity<ApiResponse<MasOutputTypeResponse>> updateOutputType(
            @PathVariable Long id,
            @RequestBody MasOutputTypeRequest request) {
        return ResponseEntity.ok( masOutputTypeService.update(id, request));
    }

    @PutMapping("masOutputType/status/{id}")
    public ResponseEntity<ApiResponse<MasOutputTypeResponse>> changeStatusOutputType(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok( masOutputTypeService.changeStatus(id, status));
    }

    //    ===============================Mas Admission Status=====================================

    @GetMapping("masAdmissionStatus/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasAdmissionStatusResponse>>> getAllAdmissionStatus(@PathVariable int flag) {
        return ResponseEntity.ok(masAdmissionStatusService.getAll(flag));
    }

    @GetMapping("masAdmissionStatus/getById/{id}")
    public ResponseEntity<ApiResponse<MasAdmissionStatusResponse>> getByIdAdmissionStatus(@PathVariable Long id) {
        return ResponseEntity.ok(masAdmissionStatusService.getById(id));
    }

    @PostMapping("masAdmissionStatus/create")
    public ResponseEntity<ApiResponse<MasAdmissionStatusResponse>> createAdmissionStatus(
            @RequestBody MasAdmissionStatusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(masAdmissionStatusService.create(request));
    }

    @PutMapping("masAdmissionStatus/update/{id}")
    public ResponseEntity<ApiResponse<MasAdmissionStatusResponse>> updateAdmissionStatus(
            @PathVariable Long id,
            @RequestBody MasAdmissionStatusRequest request) {
        return ResponseEntity.ok(masAdmissionStatusService.update(id, request));
    }

    @PutMapping("masAdmissionStatus/status/{id}")
    public ResponseEntity<ApiResponse<MasAdmissionStatusResponse>> changeStatusAdmissionStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masAdmissionStatusService.changeStatus(id, status));
    }

    //    ===============================Mas Patient Acuity=====================================

    @GetMapping("masPatientAcuity/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasPatientAcuityResponse>>> getAllPatientAcuity(@PathVariable int flag) {
        return ResponseEntity.ok( masPatientAcuityService.getAll(flag));
    }

    @GetMapping("masPatientAcuity/getById/{id}")
    public ResponseEntity<ApiResponse<MasPatientAcuityResponse>> getByIdPatientAcuity(@PathVariable Long id) {
        return ResponseEntity.ok( masPatientAcuityService.getById(id));
    }

    @PostMapping("masPatientAcuity/create")
    public ResponseEntity<ApiResponse<MasPatientAcuityResponse>> createPatientAcuity(@RequestBody MasPatientAcuityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body( masPatientAcuityService.create(request));
    }

    @PutMapping("masPatientAcuity/update/{id}")
    public ResponseEntity<ApiResponse<MasPatientAcuityResponse>> updatePatientAcuity(
            @PathVariable Long id,
            @RequestBody MasPatientAcuityRequest request) {
        return ResponseEntity.ok( masPatientAcuityService.update(id, request));
    }

    @PutMapping("masPatientAcuity/status/{id}")
    public ResponseEntity<ApiResponse<MasPatientAcuityResponse>> updateStatusPatientAcuity(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok( masPatientAcuityService.changeStatus(id, status));
    }


    //    =============================== Mas Opd Medical Advise=====================================

    @GetMapping("masOpdMedicalAdvise/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasOpdMedicalAdviseResponse>>> getAllMasOpdMedicalAdvise(@PathVariable int flag) {
        return ResponseEntity.ok( masOpdMedicalAdviseService.getAll(flag));
    }
    @PostMapping("masOpdMedicalAdvise/create")
    public ResponseEntity<ApiResponse<MasOpdMedicalAdviseResponse>>
    createOpdMedicalAdvise(@RequestBody MasOpdMedicalAdviseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masOpdMedicalAdviseService.create(request));
    }

    @PutMapping("masOpdMedicalAdvise/update/{id}")
    public ResponseEntity<ApiResponse<MasOpdMedicalAdviseResponse>>
    updateOpdMedicalAdvise(@PathVariable Long id,
           @RequestBody MasOpdMedicalAdviseRequest request) {
        return ResponseEntity.ok(masOpdMedicalAdviseService.update(id, request));
    }

    @PutMapping("masOpdMedicalAdvise/status/{id}")
    public ResponseEntity<ApiResponse<MasOpdMedicalAdviseResponse>>
    changeStatusOpdMedicalAdvise(@PathVariable Long id,
                 @RequestParam String status) {
        return ResponseEntity.ok(masOpdMedicalAdviseService.changeStatus(id, status));
    }

    //    ===============================Mas Specialty Center=====================================

    @GetMapping("masSpecialty/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasSpecialtyCenterResponse>>> getAllSpecialty(
            @PathVariable int flag) {
        return ResponseEntity.ok( masSpecialtyCenterService.getAll(flag));
    }

    @GetMapping("masSpecialty/getById/{id}")
    public ResponseEntity<ApiResponse<MasSpecialtyCenterResponse>> getByIdSpecialty(
            @PathVariable Long id) {
        return ResponseEntity.ok( masSpecialtyCenterService.getById(id));
    }

    @PostMapping("masSpecialty/create")
    public ResponseEntity<ApiResponse<MasSpecialtyCenterResponse>> createSpecialty(
            @RequestBody MasSpecialtyCenterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body( masSpecialtyCenterService.create(request));
    }

    @PutMapping("masSpecialty/update/{id}")
    public ResponseEntity<ApiResponse<MasSpecialtyCenterResponse>> updateSpecialty(
            @PathVariable Long id,
            @RequestBody MasSpecialtyCenterRequest request) {
        return ResponseEntity.ok( masSpecialtyCenterService.update(id, request));
    }

    @PutMapping("masSpecialty/status/{id}")
    public ResponseEntity<ApiResponse<MasSpecialtyCenterResponse>> changeStatusSpecialty(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok( masSpecialtyCenterService.changeStatus(id, status));
    }

    //    ===============================Mas Designation=====================================


    @GetMapping("masDesignation/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasDesignationResponse>>> getAllMasDesignation(
            @PathVariable int flag) {
        return ResponseEntity.ok(masDesignationService.getAll(flag));
    }

    @GetMapping("masDesignation/getById/{id}")
    public ResponseEntity<ApiResponse<List<MasDesignationResponse>>> getByIdMasDesignation(
            @PathVariable Long id) {
        return ResponseEntity.ok(masDesignationService.getById(id));
    }

    @PostMapping("masDesignation/create")
    public ResponseEntity<ApiResponse<MasDesignationResponse>> createMasDesignation(
            @RequestBody MasDesignationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masDesignationService.create(request));
    }

    @PutMapping("masDesignation/update/{id}")
    public ResponseEntity<ApiResponse<MasDesignationResponse>> updateMasDesignation(
            @PathVariable Long id,
            @RequestBody MasDesignationRequest request) {
        return ResponseEntity.ok(masDesignationService.update(id, request));
    }

    @PutMapping("masDesignation/status/{id}")
    public ResponseEntity<ApiResponse<MasDesignationResponse>> changeStatusMasDesignation(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masDesignationService.changeStatus(id, status));
    }
    //    ===============================Billing Policy=====================================

    @GetMapping("billingPolicy/getAll")
    public ResponseEntity<ApiResponse<List<BillingPolicyResponse>>> getAllPolicy() {
        return ResponseEntity.ok(billingPolicyService.getAll());
    }

    @GetMapping("billingPolicy/getById/{id}")
    public ResponseEntity<ApiResponse<BillingPolicyResponse>> getByIdPolicy(
            @PathVariable Long id) {
        return ResponseEntity.ok(billingPolicyService.getById(id));
    }

    @PostMapping("billingPolicy/create")
    public ResponseEntity<ApiResponse<BillingPolicyResponse>> createPolicy(
            @RequestBody BillingPolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingPolicyService.create(request));
    }

    @PutMapping("billingPolicy/update/{id}")
    public ResponseEntity<ApiResponse<BillingPolicyResponse>> updatePolicy(
            @PathVariable Long id,
            @RequestBody BillingPolicyRequest request) {
        return ResponseEntity.ok(billingPolicyService.update(id, request));
    }

//    @PutMapping("billingPolicy/status/{id}")
//    public ResponseEntity<ApiResponse<BillingPolicyResponse>> changeStatusPolicy(
//            @PathVariable Long id,
//            @RequestParam String status) {
//        return ResponseEntity.ok(billingPolicyService.changeStatus(id, status));
//    }

    //    ===============================Mas Nursing Type=====================================
    @GetMapping("masNursingType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasNursingTypeResponse>>> getAllNursingType(@PathVariable int flag) {
        return ResponseEntity.ok(masNursingTypeService.getAll(flag));
    }

    @GetMapping("masNursingType/getById/{id}")
    public ResponseEntity<ApiResponse<MasNursingTypeResponse>> getByIdNursingType(@PathVariable Long id) {
        return ResponseEntity.ok(masNursingTypeService.getById(id));
    }

    @PostMapping("masNursingType/create")
    public ResponseEntity<ApiResponse<MasNursingTypeResponse>> createNursingType(@RequestBody MasNursingTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masNursingTypeService.create(request));
    }

    @PutMapping("masNursingType/update/{id}")
    public ResponseEntity<ApiResponse<MasNursingTypeResponse>> updateNursingType(@PathVariable Long id,
           @RequestBody MasNursingTypeRequest request) {
        return ResponseEntity.ok(masNursingTypeService.update(id, request));
    }

    @PutMapping("masNursingType/status/{id}")
    public ResponseEntity<ApiResponse<MasNursingTypeResponse>> changeStatusNursingType(@PathVariable Long id,
                 @RequestParam String status) {
        return ResponseEntity.ok(masNursingTypeService.changeStatus(id, status));
    }
    //    ===============================Mas Tooth=====================================

    @GetMapping("masTooth/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasToothMasterResponse>>> getAllTooth(@PathVariable int flag) {
        return ResponseEntity.ok(masToothMasterService.getAll(flag));
    }

    @GetMapping("masTooth/getById/{id}")
    public ResponseEntity<ApiResponse<MasToothMasterResponse>> getByIdTooth(@PathVariable Long id) {
        return ResponseEntity.ok(masToothMasterService.getById(id));
    }

    @PostMapping("masTooth/create")
    public ResponseEntity<ApiResponse<MasToothMasterResponse>> createTooth(@RequestBody MasToothMasterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masToothMasterService.create(request));
    }

    @PutMapping("masTooth/update/{id}")
    public ResponseEntity<ApiResponse<MasToothMasterResponse>> updateTooth(@PathVariable Long id,
           @RequestBody MasToothMasterRequest request) {
        return ResponseEntity.ok(masToothMasterService.update(id, request));
    }

    @PutMapping("masTooth/status/{id}")
    public ResponseEntity<ApiResponse<MasToothMasterResponse>> changeStatusTooth(@PathVariable Long id,
                 @RequestParam String status) {
        return ResponseEntity.ok(masToothMasterService.changeStatus(id, status));
    }

    //    ===============================Mas Tooth Condition=====================================

    @GetMapping("masToothCondition/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasToothConditionResponse>>> getAllToothCondition(@PathVariable int flag) {
        return ResponseEntity.ok(masToothConditionService.getAll(flag));
    }

    @GetMapping("masToothCondition/getById/{id}")
    public ResponseEntity<ApiResponse<MasToothConditionResponse>> getByIdToothCondition(@PathVariable Long id) {
        return ResponseEntity.ok(masToothConditionService.getById(id));
    }

    @PostMapping("masToothCondition/create")
    public ResponseEntity<ApiResponse<MasToothConditionResponse>> createToothCondition(@RequestBody MasToothConditionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masToothConditionService.create(request));
    }

    @PutMapping("masToothCondition/update/{id}")
    public ResponseEntity<ApiResponse<MasToothConditionResponse>> updateToothCondition(@PathVariable Long id,
           @RequestBody MasToothConditionRequest request) {
        return ResponseEntity.ok(masToothConditionService.update(id, request));
    }

    @PutMapping("masToothCondition/status/{id}")
    public ResponseEntity<ApiResponse<MasToothConditionResponse>> changeStatusToothCondition(@PathVariable Long id,
                 @RequestParam String status) {
        return ResponseEntity.ok(masToothConditionService.changeStatus(id, status));
    }

    //    ===============================Opth Mas Distance Vision=====================================

    @GetMapping("opthMasDistanceVision/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<OpthMasDistanceVisionResponse>>> getAllOpthDistance(@PathVariable int flag) {
        return ResponseEntity.ok(opthMasDistanceVisionService.getAll(flag));
    }

    @GetMapping("opthMasDistanceVision/getById/{id}")
    public ResponseEntity<ApiResponse<OpthMasDistanceVisionResponse>> getByIdOpthDistance(@PathVariable Long id) {
        return ResponseEntity.ok(opthMasDistanceVisionService.getById(id));
    }

    @PostMapping("opthMasDistanceVision/create")
    public ResponseEntity<ApiResponse<OpthMasDistanceVisionResponse>> createOpthDistance(@RequestBody OpthMasDistanceVisionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opthMasDistanceVisionService.create(request));
    }

    @PutMapping("opthMasDistanceVision/update/{id}")
    public ResponseEntity<ApiResponse<OpthMasDistanceVisionResponse>> updateOpthDistance(@PathVariable Long id,
           @RequestBody OpthMasDistanceVisionRequest request) {
        return ResponseEntity.ok(opthMasDistanceVisionService.update(id, request));
    }

    @PutMapping("opthMasDistanceVision/status/{id}")
    public ResponseEntity<ApiResponse<OpthMasDistanceVisionResponse>> changeStatusOpthDistance(@PathVariable Long id,
                 @RequestParam String status) {
        return ResponseEntity.ok(opthMasDistanceVisionService.changeStatus(id, status));
    }

    //    ===============================Opth Mas Near Vision=====================================


    @GetMapping("opthMasNearVision/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<OphthMasNearVisionResponse>>> getAllOpthNear(
            @PathVariable int flag) {

        return ResponseEntity.ok( ophthMasNearVisionService.getAll(flag));
    }

    @GetMapping("opthMasNearVision/getById/{id}")
    public ResponseEntity<ApiResponse<OphthMasNearVisionResponse>> getByIdOpthNear(
            @PathVariable Long id) {

        return ResponseEntity.ok( ophthMasNearVisionService.getById(id));
    }

    @PostMapping("opthMasNearVision/create")
    public ResponseEntity<ApiResponse<OphthMasNearVisionResponse>> createOpthNear(
            @RequestBody OphthMasNearVisionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body( ophthMasNearVisionService.create(request));
    }

    @PutMapping("opthMasNearVision/update/{id}")
    public ResponseEntity<ApiResponse<OphthMasNearVisionResponse>> updateOpthNear(
            @PathVariable Long id,
            @RequestBody OphthMasNearVisionRequest request) {

        return ResponseEntity.ok(
                ophthMasNearVisionService.update(id, request)
        );
    }

    @PutMapping("opthMasNearVision/status/{id}")
    public ResponseEntity<ApiResponse<OphthMasNearVisionResponse>> changeStatusOpthNear(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                ophthMasNearVisionService.changeStatus(id, status)
        );
    }

    //    ===============================Opth Mas Color Vision=====================================

    @GetMapping("opthMasColorVision/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<OpthMasColorVisionResponse>>> getAllColorVision(
            @PathVariable int flag) {
        return ResponseEntity.ok(opthMasColorVisionService.getAll(flag));
    }

    @GetMapping("opthMasColorVision/getById/{id}")
    public ResponseEntity<ApiResponse<OpthMasColorVisionResponse>> getByIdColorVision(
            @PathVariable Long id) {
        return ResponseEntity.ok(opthMasColorVisionService.getById(id));
    }

    @PostMapping("opthMasColorVision/create")
    public ResponseEntity<ApiResponse<OpthMasColorVisionResponse>> createColorVision(
            @RequestBody OpthMasColorVisionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opthMasColorVisionService.create(request));
    }

    @PutMapping("opthMasColorVision/update/{id}")
    public ResponseEntity<ApiResponse<OpthMasColorVisionResponse>> updateColorVision(
            @PathVariable Long id,
            @RequestBody OpthMasColorVisionRequest request) {

        return ResponseEntity.ok(opthMasColorVisionService.update(id, request));
    }

    @PutMapping("opthMasColorVision/status/{id}")
    public ResponseEntity<ApiResponse<OpthMasColorVisionResponse>> changeStatusColorVision(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(opthMasColorVisionService.changeStatus(id, status));
    }

    //    ===============================Opth Mas Spectacle Use =====================================

    @GetMapping("opthMasSpectacleUse/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<OpthMasSpectacleUseResponse>>> getAllSpectacleUse(
            @PathVariable int flag) {
        return ResponseEntity.ok(masSpectacleUseService.getAll(flag));
    }

    @GetMapping("opthMasSpectacleUse/getById/{id}")
    public ResponseEntity<ApiResponse<OpthMasSpectacleUseResponse>> getByIdSpectacleUse(
            @PathVariable Long id) {
        return ResponseEntity.ok(masSpectacleUseService.getById(id));
    }

    @PostMapping("opthMasSpectacleUse/create")
    public ResponseEntity<ApiResponse<OpthMasSpectacleUseResponse>> creatSpectacleUsee(
            @RequestBody OpthMasSpectacleUseRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masSpectacleUseService.create(request));
    }

    @PutMapping("opthMasSpectacleUse/update/{id}")
    public ResponseEntity<ApiResponse<OpthMasSpectacleUseResponse>> updateSpectacleUse(
            @PathVariable Long id,
            @RequestBody OpthMasSpectacleUseRequest request) {

        return ResponseEntity.ok(masSpectacleUseService.update(id, request));
    }

    @PutMapping("opthMasSpectacleUse/status/{id}")
    public ResponseEntity<ApiResponse<OpthMasSpectacleUseResponse>> changeStatusSpectacleUse(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(masSpectacleUseService.changeStatus(id, status));
    }
    //    ===============================Opth Mas Lens Type =====================================

    @GetMapping("opthMasLensType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<OpthMasLensTypeResponse>>> getAllLensType(
            @PathVariable int flag) {
        return ResponseEntity.ok(lensTypeService.getAll(flag));
    }

    @GetMapping("opthMasLensType/getById/{id}")
    public ResponseEntity<ApiResponse<OpthMasLensTypeResponse>> getByIdLensType(
            @PathVariable Long id) {
        return ResponseEntity.ok(lensTypeService.getById(id));
    }

    @PostMapping("opthMasLensType/create")
    public ResponseEntity<ApiResponse<OpthMasLensTypeResponse>> createLensType(
            @RequestBody OpthMasLensTypeRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lensTypeService.create(request));
    }

    @PutMapping("opthMasLensType/update/{id}")
    public ResponseEntity<ApiResponse<OpthMasLensTypeResponse>> updateLensType(
            @PathVariable Long id,
            @RequestBody OpthMasLensTypeRequest request) {

        return ResponseEntity.ok(
                lensTypeService.update(id, request));
    }

    @PutMapping("opthMasLensType/status/{id}")
    public ResponseEntity<ApiResponse<OpthMasLensTypeResponse>> changeStatusLensType(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                lensTypeService.changeStatus(id, status));
    }

    //    ===============================Ob Mas Conception=====================================



    @GetMapping("obMasConception/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasConceptionResponse>>> getAllMasConception(
            @PathVariable int flag) {

        return ResponseEntity.ok(obMasConceptionService.getAll(flag));
    }

    @GetMapping("obMasConception/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasConceptionResponse>> getByIdMasConception(
            @PathVariable Long id) {

        return ResponseEntity.ok(obMasConceptionService.getById(id));
    }

    @PostMapping("obMasConception/create")
    public ResponseEntity<ApiResponse<ObMasConceptionResponse>> createMasConception(
            @RequestBody ObMasConceptionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasConceptionService.create(request));
    }

    @PutMapping("obMasConception/update/{id}")
    public ResponseEntity<ApiResponse<ObMasConceptionResponse>> updateMasConception(
            @PathVariable Long id,
            @RequestBody ObMasConceptionRequest request) {

        return ResponseEntity.ok(obMasConceptionService.update(id, request));
    }

    @PutMapping("obMasConception/status/{id}")
    public ResponseEntity<ApiResponse<ObMasConceptionResponse>> changeStatusMasConception(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasConceptionService.changeStatus(id, status));
    }

    //    ===============================Ob Mas Consanguinity=====================================

    @GetMapping("obMasConsanguinity/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasConsanguinityResponse>>> getAllMasConsanguinity(
            @PathVariable int flag) {

        return ResponseEntity.ok(obMasConsanguinityService.getAll(flag));
    }

    @GetMapping("obMasConsanguinity/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasConsanguinityResponse>> getByIdMasConsanguinity(
            @PathVariable Long id) {

        return ResponseEntity.ok(obMasConsanguinityService.getById(id));
    }

    @PostMapping("obMasConsanguinity/create")
    public ResponseEntity<ApiResponse<ObMasConsanguinityResponse>> createMasConsanguinity(
            @RequestBody ObMasConsanguinityRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasConsanguinityService.create(request));
    }

    @PutMapping("obMasConsanguinity/update/{id}")
    public ResponseEntity<ApiResponse<ObMasConsanguinityResponse>> updateMasConsanguinity(
            @PathVariable Long id,
            @RequestBody ObMasConsanguinityRequest request) {

        return ResponseEntity.ok(obMasConsanguinityService.update(id, request));
    }

    @PutMapping("obMasConsanguinity/status/{id}")
    public ResponseEntity<ApiResponse<ObMasConsanguinityResponse>> changeStatusMasConsanguinity(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasConsanguinityService.changeStatus(id, status));
    }

    //    ===============================Ob Mas Booked Status=====================================

    @GetMapping("obMasBookedStatus/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasBookedStatusResponse>>> getAllMasBookedStatus(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasBookedStatusService.getAll(flag));
    }

    @GetMapping("obMasBookedStatus/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasBookedStatusResponse>> getByIdMasBookedStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasBookedStatusService.getById(id));
    }

    @PostMapping("obMasBookedStatus/create")
    public ResponseEntity<ApiResponse<ObMasBookedStatusResponse>> createMasBookedStatus(
            @RequestBody ObMasBookedStatusRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasBookedStatusService.create(request));
    }

    @PutMapping("obMasBookedStatus/update/{id}")
    public ResponseEntity<ApiResponse<ObMasBookedStatusResponse>> updateMasBookedStatus(
            @PathVariable Long id,
            @RequestBody ObMasBookedStatusRequest request) {

        return ResponseEntity.ok(obMasBookedStatusService.update(id, request));
    }

    @PutMapping("obMasBookedStatus/status/{id}")
    public ResponseEntity<ApiResponse<ObMasBookedStatusResponse>> changeStatusMasBookedStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasBookedStatusService.changeStatus(id, status));
    }
    //    ===============================Ob Mas Immunised Status=====================================

    @GetMapping("obMasImmunisedStatus/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasImmunisedStatusResponse>>> getAllMasImmunisedStatus(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasImmunisedStatusService.getAll(flag));
    }

    @GetMapping("obMasImmunisedStatus/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasImmunisedStatusResponse>> getByIdMasImmunisedStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasImmunisedStatusService.getById(id));
    }

    @PostMapping("obMasImmunisedStatus/create")
    public ResponseEntity<ApiResponse<ObMasImmunisedStatusResponse>> createMasImmunisedStatus(
            @RequestBody ObMasImmunisedStatusRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasImmunisedStatusService.create(request));
    }

    @PutMapping("obMasImmunisedStatus/update/{id}")
    public ResponseEntity<ApiResponse<ObMasImmunisedStatusResponse>> updateMasImmunisedStatus(
            @PathVariable Long id,
            @RequestBody ObMasImmunisedStatusRequest request) {

        return ResponseEntity.ok(obMasImmunisedStatusService.update(id, request));
    }

    @PutMapping("obMasImmunisedStatus/status/{id}")
    public ResponseEntity<ApiResponse<ObMasImmunisedStatusResponse>> changeStatusMasImmunisedStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasImmunisedStatusService.changeStatus(id, status));
    }

    //    ===============================Ob Mas Trimester=====================================


    @GetMapping("obMasTrimester/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasTrimesterResponse>>> getAllMasTrimester(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasTrimesterService.getAll(flag));
    }

    @GetMapping("obMasTrimester/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasTrimesterResponse>> getByIdMasTrimester(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasTrimesterService.getById(id));
    }

    @PostMapping("obMasTrimester/create")
    public ResponseEntity<ApiResponse<ObMasTrimesterResponse>> createMasTrimester(
            @RequestBody ObMasTrimesterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasTrimesterService.create(request));
    }

    @PutMapping("obMasTrimester/update/{id}")
    public ResponseEntity<ApiResponse<ObMasTrimesterResponse>> updateMasTrimester(
            @PathVariable Long id,
            @RequestBody ObMasTrimesterRequest request) {

        return ResponseEntity.ok(obMasTrimesterService.update(id, request));
    }

    @PutMapping("obMasTrimester/status/{id}")
    public ResponseEntity<ApiResponse<ObMasTrimesterResponse>> changeStatusMasTrimester(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasTrimesterService.changeStatus(id, status));
    }

    //    ===============================Ob Mas Presentation=====================================


    @GetMapping("obMasPresentation/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasPresentationResponse>>> getAllMasPresentation(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasPresentationService.getAll(flag));
    }

    @GetMapping("obMasPresentation/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasPresentationResponse>> getByIdMasPresentation(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasPresentationService.getById(id));
    }

    @PostMapping("obMasPresentation/create")
    public ResponseEntity<ApiResponse<ObMasPresentationResponse>> createMasPresentation(
            @RequestBody ObMasPresentationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasPresentationService.create(request));
    }

    @PutMapping("obMasPresentation/update/{id}")
    public ResponseEntity<ApiResponse<ObMasPresentationResponse>> updateMasPresentation(
            @PathVariable Long id,
            @RequestBody ObMasPresentationRequest request) {

        return ResponseEntity.ok(obMasPresentationService.update(id, request));
    }

    @PutMapping("obMasPresentation/status/{id}")
    public ResponseEntity<ApiResponse<ObMasPresentationResponse>> changeStatusMasPresentation(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasPresentationService.changeStatus(id, status));
    }
//    ===============================Ob Mas PvMembrane=====================================



    @GetMapping("ObMasPvMembrane/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasPvMembraneResponse>>> getAllObMasPvMembrane(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasPvMembraneService.getAll(flag));
    }

    @GetMapping("ObMasPvMembrane/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasPvMembraneResponse>> getByIdObMasPvMembrane(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasPvMembraneService.getById(id));
    }

    @PostMapping("ObMasPvMembrane/create")
    public ResponseEntity<ApiResponse<ObMasPvMembraneResponse>> createObMasPvMembrane(
            @RequestBody @Valid ObMasPvMembraneRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasPvMembraneService.create(request));
    }

    @PutMapping("ObMasPvMembrane/update/{id}")
    public ResponseEntity<ApiResponse<ObMasPvMembraneResponse>> updateObMasPvMembrane(
            @PathVariable Long id,
            @RequestBody @Valid ObMasPvMembraneRequest request) {

        return ResponseEntity.ok(obMasPvMembraneService.update(id, request));
    }

    @PutMapping("ObMasPvMembrane/status/{id}")
    public ResponseEntity<ApiResponse<ObMasPvMembraneResponse>> changeStatusObMasPvMembrane(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasPvMembraneService.changeStatus(id, status));
    }

//    ===============================Ob Mas PvLiquor=====================================


    @GetMapping("ObMasPvLiquor/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasPvLiquorResponse>>> getAllPvLiquor(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasPvLiquorService.getAll(flag));
    }

    @GetMapping("ObMasPvLiquor/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasPvLiquorResponse>> getByIdPvLiquor(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasPvLiquorService.getById(id));
    }

    @PostMapping("ObMasPvLiquor/create")
    public ResponseEntity<ApiResponse<ObMasPvLiquorResponse>> createPvLiquor(
            @RequestBody @Valid ObMasPvLiquorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasPvLiquorService.create(request));
    }

    @PutMapping("ObMasPvLiquor/update/{id}")
    public ResponseEntity<ApiResponse<ObMasPvLiquorResponse>> updatePvLiquor(
            @PathVariable Long id,
            @RequestBody @Valid ObMasPvLiquorRequest request) {
        return ResponseEntity.ok(obMasPvLiquorService.update(id, request));
    }

    @PutMapping("ObMasPvLiquor/status/{id}")
    public ResponseEntity<ApiResponse<ObMasPvLiquorResponse>> changeStatusPvLiquor(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(obMasPvLiquorService.changeStatus(id, status));
    }
//    ===============================Ob Mas Cervix Consistency=================================

    @GetMapping("ObMasCervixConsistency/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasCervixConsistencyResponse>>> getAllConsistency(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasCervixConsistencyService.getAll(flag));
    }

    @GetMapping("ObMasCervixConsistency/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasCervixConsistencyResponse>> getByIdConsistency(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasCervixConsistencyService.getById(id));
    }

    @PostMapping("ObMasCervixConsistency/create")
    public ResponseEntity<ApiResponse<ObMasCervixConsistencyResponse>> createConsistency(
            @RequestBody @Valid ObMasCervixConsistencyRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasCervixConsistencyService.create(request));
    }

    @PutMapping("ObMasCervixConsistency/update/{id}")
    public ResponseEntity<ApiResponse<ObMasCervixConsistencyResponse>> updateConsistency(
            @PathVariable Long id,
            @RequestBody @Valid ObMasCervixConsistencyRequest request) {

        return ResponseEntity.ok(obMasCervixConsistencyService.update(id, request));
    }

    @PutMapping("ObMasCervixConsistency/status/{id}")
    public ResponseEntity<ApiResponse<ObMasCervixConsistencyResponse>> changeStatusConsistency(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasCervixConsistencyService.changeStatus(id, status));
    }

//    ===============================Ob Mas Cervix Position=================================

    @GetMapping("ObMasCervixPosition/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasCervixPositionResponse>>> getAllCervixPosition(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasCervixPositionService.getAll(flag));
    }

    @GetMapping("ObMasCervixPosition/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasCervixPositionResponse>> getByIdCervixPosition(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasCervixPositionService.getById(id));
    }

    @PostMapping("ObMasCervixPosition/create")
    public ResponseEntity<ApiResponse<ObMasCervixPositionResponse>> createCervixPosition(
            @RequestBody @Valid ObMasCervixPositionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasCervixPositionService.create(request));
    }

    @PutMapping("ObMasCervixPosition/update/{id}")
    public ResponseEntity<ApiResponse<ObMasCervixPositionResponse>> updateCervixPosition(
            @PathVariable Long id,
            @RequestBody @Valid ObMasCervixPositionRequest request) {

        return ResponseEntity.ok(obMasCervixPositionService.update(id, request));
    }

    @PutMapping("ObMasCervixPosition/status/{id}")
    public ResponseEntity<ApiResponse<ObMasCervixPositionResponse>> changeStatusCervixPosition(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(obMasCervixPositionService.changeStatus(id, status));
    }


//    ===============================Ob Mas Station Presenting ====================


    @GetMapping("obMasStationPresenting/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasStationPresentingResponse>>> getAllPresenting(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasStationPresentingService.getAll(flag));
    }

    @GetMapping("obMasStationPresenting/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasStationPresentingResponse>> getByIdPresenting(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasStationPresentingService.getById(id));
    }

    @PostMapping("obMasStationPresenting/create")
    public ResponseEntity<ApiResponse<ObMasStationPresentingResponse>> createPresenting(
            @RequestBody @Valid ObMasStationPresentingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasStationPresentingService.create(request));
    }

    @PutMapping("obMasStationPresenting/update/{id}")
    public ResponseEntity<ApiResponse<ObMasStationPresentingResponse>> updatePresenting(
            @PathVariable Long id,
            @RequestBody @Valid ObMasStationPresentingRequest request) {
        return ResponseEntity.ok(obMasStationPresentingService.update(id, request));
    }

    @PutMapping("obMasStationPresenting/status/{id}")
    public ResponseEntity<ApiResponse<ObMasStationPresentingResponse>> changeStatusPresenting(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(obMasStationPresentingService.changeStatus(id, status));
    }

//    ===============================Ob Mas Pelvis Type====================


    @GetMapping("obMasPelvisType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<ObMasPelvisTypeResponse>>> getAllMasPelvisType(
            @PathVariable int flag) {
        return ResponseEntity.ok(obMasPelvisTypeService.getAll(flag));
    }

    @GetMapping("obMasPelvisType/getById/{id}")
    public ResponseEntity<ApiResponse<ObMasPelvisTypeResponse>> getByIdMasPelvisType(
            @PathVariable Long id) {
        return ResponseEntity.ok(obMasPelvisTypeService.getById(id));
    }

    @PostMapping("obMasPelvisType/create")
    public ResponseEntity<ApiResponse<ObMasPelvisTypeResponse>> createMasPelvisType(
            @RequestBody @Valid ObMasPelvisTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obMasPelvisTypeService.create(request));
    }

    @PutMapping("obMasPelvisType/update/{id}")
    public ResponseEntity<ApiResponse<ObMasPelvisTypeResponse>> updateMasPelvisType(
            @PathVariable Long id,
            @RequestBody @Valid ObMasPelvisTypeRequest request) {
        return ResponseEntity.ok(obMasPelvisTypeService.update(id, request));
    }

    @PutMapping("obMasPelvisType/status/{id}")
    public ResponseEntity<ApiResponse<ObMasPelvisTypeResponse>> changeStatusMasPelvisType(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(obMasPelvisTypeService.changeStatus(id, status));
    }
//    ===============================Gyn Mas Flow====================

    @GetMapping("gynMasFlow/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<GynMasFlowResponse>>> getAllFlow(
            @PathVariable int flag) {
        return ResponseEntity.ok(gynMasFlowService.getAll(flag));
    }

    @GetMapping("gynMasFlow/getById/{id}")
    public ResponseEntity<ApiResponse<GynMasFlowResponse>> getByIdFlow(
            @PathVariable Long id) {
        return ResponseEntity.ok(gynMasFlowService.getById(id));
    }

    @PostMapping("gynMasFlow/create")
    public ResponseEntity<ApiResponse<GynMasFlowResponse>> createFlow(
            @RequestBody @Valid GynMasFlowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gynMasFlowService.create(request));
    }

    @PutMapping("gynMasFlow/update/{id}")
    public ResponseEntity<ApiResponse<GynMasFlowResponse>> updateFlow(
            @PathVariable Long id,
            @RequestBody @Valid GynMasFlowRequest request) {
        return ResponseEntity.ok(gynMasFlowService.update(id, request));
    }

    @PutMapping("gynMasFlow/status/{id}")
    public ResponseEntity<ApiResponse<GynMasFlowResponse>> changeStatusFlow(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(gynMasFlowService.changeStatus(id, status));
    }

//    ===============================Gyn Mas Menarche Age====================

    @GetMapping("gynMasMenarcheAge/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<GynMasMenarcheAgeResponse>>> getAllMasMenarcheAge(
            @PathVariable int flag) {
        return ResponseEntity.ok(gynMasMenarcheAgeService.getAll(flag));
    }

    @GetMapping("gynMasMenarcheAge/getById/{id}")
    public ResponseEntity<ApiResponse<GynMasMenarcheAgeResponse>> getByIdMasMenarcheAge(
            @PathVariable Long id) {
        return ResponseEntity.ok(gynMasMenarcheAgeService.getById(id));
    }

    @PostMapping("gynMasMenarcheAge/create")
    public ResponseEntity<ApiResponse<GynMasMenarcheAgeResponse>> createMasMenarcheAge(
            @RequestBody @Valid GynMasMenarcheAgeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gynMasMenarcheAgeService.create(request));
    }

    @PutMapping("gynMasMenarcheAge/update/{id}")
    public ResponseEntity<ApiResponse<GynMasMenarcheAgeResponse>> updateMasMenarcheAge(
            @PathVariable Long id,
            @RequestBody @Valid GynMasMenarcheAgeRequest request) {
        return ResponseEntity.ok(gynMasMenarcheAgeService.update(id, request));
    }

    @PutMapping("gynMasMenarcheAge/status/{id}")
    public ResponseEntity<ApiResponse<GynMasMenarcheAgeResponse>> changeStatusMasMenarcheAge(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(gynMasMenarcheAgeService.changeStatus(id, status));
    }

//    ===============================Gyn Mas Menstrual Pattern ====================

    @GetMapping("gynMasMenstrualPattern/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<GynMasMenstrualPatternResponse>>> getAllMasMenstrualPattern(
            @PathVariable int flag) {
        return ResponseEntity.ok(gynMasMenstrualPatternService.getAll(flag));
    }

    @GetMapping("gynMasMenstrualPattern/getById/{id}")
    public ResponseEntity<ApiResponse<GynMasMenstrualPatternResponse>> getByIdMasMenstrualPattern(
            @PathVariable Long id) {
        return ResponseEntity.ok(gynMasMenstrualPatternService.getById(id));
    }

    @PostMapping("gynMasMenstrualPattern/create")
    public ResponseEntity<ApiResponse<GynMasMenstrualPatternResponse>> createMasMenstrualPattern(
            @RequestBody @Valid GynMasMenstrualPatternRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gynMasMenstrualPatternService.create(request));
    }

    @PutMapping("gynMasMenstrualPattern/update/{id}")
    public ResponseEntity<ApiResponse<GynMasMenstrualPatternResponse>> updateMasMenstrualPattern(
            @PathVariable Long id,
            @RequestBody @Valid GynMasMenstrualPatternRequest request) {
        return ResponseEntity.ok(gynMasMenstrualPatternService.update(id, request));
    }

    @PutMapping("gynMasMenstrualPattern/status/{id}")
    public ResponseEntity<ApiResponse<GynMasMenstrualPatternResponse>> changeStatusMasMenstrualPattern(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(gynMasMenstrualPatternService.changeStatus(id, status));
    }
//    ===============================Gyn Mas Sterilisation ====================

    @GetMapping("gynMasSterilisation/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<GynMasSterilisationResponse>>> getAllSterilisation(
            @PathVariable int flag) {
        return ResponseEntity.ok(gynMasSterilisationService.getAll(flag));
    }

    @GetMapping("gynMasSterilisation/getById/{id}")
    public ResponseEntity<ApiResponse<GynMasSterilisationResponse>> getByIdSterilisation(
            @PathVariable Long id) {
        return ResponseEntity.ok(gynMasSterilisationService.getById(id));
    }

    @PostMapping("gynMasSterilisation/create")
    public ResponseEntity<ApiResponse<GynMasSterilisationResponse>> createSterilisation(
            @RequestBody @Valid GynMasSterilisationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gynMasSterilisationService.create(request));
    }

    @PutMapping("gynMasSterilisation/update/{id}")
    public ResponseEntity<ApiResponse<GynMasSterilisationResponse>> updateSterilisation(
            @PathVariable Long id,
            @RequestBody @Valid GynMasSterilisationRequest request) {
        return ResponseEntity.ok(gynMasSterilisationService.update(id, request));
    }

    @PutMapping("gynMasSterilisation/status/{id}")
    public ResponseEntity<ApiResponse<GynMasSterilisationResponse>> changeStatusSterilisation(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(gynMasSterilisationService.changeStatus(id, status));
    }


//    ===============================Gyn Mas Pap Smear ====================

    @GetMapping("gynMasPapSmear/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<GynMasPapSmearResponse>>> getAllMasPapSmear(
            @PathVariable int flag) {
        return ResponseEntity.ok(gynMasPapSmearService.getAll(flag));
    }

    @GetMapping("gynMasPapSmear/getById/{id}")
    public ResponseEntity<ApiResponse<GynMasPapSmearResponse>> getByIdMasPapSmear(
            @PathVariable Long id) {
        return ResponseEntity.ok(gynMasPapSmearService.getById(id));
    }

    @PostMapping("gynMasPapSmear/create")
    public ResponseEntity<ApiResponse<GynMasPapSmearResponse>> createMasPapSmear(
            @RequestBody @Valid GynMasPapSmearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gynMasPapSmearService.create(request));
    }

    @PutMapping("gynMasPapSmear/update/{id}")
    public ResponseEntity<ApiResponse<GynMasPapSmearResponse>> updateMasPapSmear(
            @PathVariable Long id,
            @RequestBody @Valid GynMasPapSmearRequest request) {
        return ResponseEntity.ok(gynMasPapSmearService.update(id, request));
    }

    @PutMapping("gynMasPapSmear/status/{id}")
    public ResponseEntity<ApiResponse<GynMasPapSmearResponse>> changeStatusMasPapSmear(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(gynMasPapSmearService.changeStatus(id, status));
    }


//    ===============================Ent Mas Pinna====================


    @GetMapping("entMasPinna/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<EntMasPinnaResponse>>> getAllEntMasPinna(
            @PathVariable int flag) {
        return ResponseEntity.ok(entMasPinnaService.getAll(flag));
    }

    @GetMapping("entMasPinna/getById/{id}")
    public ResponseEntity<ApiResponse<EntMasPinnaResponse>> getByIdEntMasPinna(
            @PathVariable Long id) {
        return ResponseEntity.ok(entMasPinnaService.getById(id));
    }

    @PostMapping("entMasPinna/create")
    public ResponseEntity<ApiResponse<EntMasPinnaResponse>> createEntMasPinna(
            @RequestBody @Valid EntMasPinnaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entMasPinnaService.create(request));
    }

    @PutMapping("entMasPinna/update/{id}")
    public ResponseEntity<ApiResponse<EntMasPinnaResponse>> updateEntMasPinna(
            @PathVariable Long id,
            @RequestBody @Valid EntMasPinnaRequest request) {
        return ResponseEntity.ok(entMasPinnaService.update(id, request));
    }

    @PutMapping("entMasPinna/status/{id}")
    public ResponseEntity<ApiResponse<EntMasPinnaResponse>> changeStatusEntMasPinna(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(entMasPinnaService.changeStatus(id, status));
    }

//    ===============================Ent Mas Ear Canal====================

    @GetMapping("entMasEarCanal/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<EntMasEarCanalResponse>>> getAllEntMasEarCanal(
            @PathVariable int flag) {
        return ResponseEntity.ok(entMasEarCanalService.getAll(flag));
    }

    @GetMapping("entMasEarCanal/getById/{id}")
    public ResponseEntity<ApiResponse<EntMasEarCanalResponse>> getByIdEntMasEarCanal(
            @PathVariable Long id) {
        return ResponseEntity.ok(entMasEarCanalService.getById(id));
    }

    @PostMapping("entMasEarCanal/create")
    public ResponseEntity<ApiResponse<EntMasEarCanalResponse>> createEntMasEarCanal(
            @RequestBody @Valid EntMasEarCanalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entMasEarCanalService.create(request));
    }

    @PutMapping("entMasEarCanal/update/{id}")
    public ResponseEntity<ApiResponse<EntMasEarCanalResponse>> updateEntMasEarCanal(
            @PathVariable Long id,
            @RequestBody @Valid EntMasEarCanalRequest request) {
        return ResponseEntity.ok(entMasEarCanalService.update(id, request));
    }

    @PutMapping("entMasEarCanal/status/{id}")
    public ResponseEntity<ApiResponse<EntMasEarCanalResponse>> changeStatusEntMasEarCanal(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(entMasEarCanalService.changeStatus(id, status));
    }


//    ===============================Ent Mas TmStatus ====================

    @GetMapping("entMasTmStatus/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<EntMasTmStatusResponse>>> getAllEntMasTmStatus(
            @PathVariable int flag) {
        return ResponseEntity.ok(entMasTmStatusService.getAll(flag));
    }

    @GetMapping("entMasTmStatus/getById/{id}")
    public ResponseEntity<ApiResponse<EntMasTmStatusResponse>> getByIdEntMasTmStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(entMasTmStatusService.getById(id));
    }

    @PostMapping("entMasTmStatus/create")
    public ResponseEntity<ApiResponse<EntMasTmStatusResponse>> createEntMasTmStatus(
            @RequestBody @Valid EntMasTmStatusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entMasTmStatusService.create(request));
    }

    @PutMapping("entMasTmStatus/update/{id}")
    public ResponseEntity<ApiResponse<EntMasTmStatusResponse>> updateEntMasTmStatus(
            @PathVariable Long id,
            @RequestBody @Valid EntMasTmStatusRequest request) {
        return ResponseEntity.ok(entMasTmStatusService.update(id, request));
    }

    @PutMapping("entMasTmStatus/status/{id}")
    public ResponseEntity<ApiResponse<EntMasTmStatusResponse>> changeStatusEntMasTmStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(entMasTmStatusService.changeStatus(id, status));
    }

//    ===============================Ent Mas Rinne ====================


    @GetMapping("entMasRinne/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<EntMasRinneResponse>>> getAllEntMasRinne(
            @PathVariable int flag) {
        return ResponseEntity.ok(entMasRinneService.getAll(flag));
    }

    @GetMapping("entMasRinne/getById/{id}")
    public ResponseEntity<ApiResponse<EntMasRinneResponse>> getByIdEntMasRinne(
            @PathVariable Long id) {
        return ResponseEntity.ok(entMasRinneService.getById(id));
    }

    @PostMapping("entMasRinne/create")
    public ResponseEntity<ApiResponse<EntMasRinneResponse>> createEntMasRinne(
            @RequestBody @Valid EntMasRinneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entMasRinneService.create(request));
    }

    @PutMapping("entMasRinne/update/{id}")
    public ResponseEntity<ApiResponse<EntMasRinneResponse>> updateEntMasRinne(
            @PathVariable Long id,
            @RequestBody @Valid EntMasRinneRequest request) {
        return ResponseEntity.ok(entMasRinneService.update(id, request));
    }

    @PutMapping("entMasRinne/status/{id}")
    public ResponseEntity<ApiResponse<EntMasRinneResponse>> changeStatusEntMasRinne(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(entMasRinneService.changeStatus(id, status));
    }

//    ===============================Ent Mas Weber ====================


    @GetMapping("entMasWeber/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<EntMasWeberResponse>>> getAllEntMasWeber(
            @PathVariable int flag) {
        return ResponseEntity.ok(entMasWeberService.getAll(flag));
    }

    @GetMapping("entMasWeber/getById/{id}")
    public ResponseEntity<ApiResponse<EntMasWeberResponse>> getByIdEntMasWeber(
            @PathVariable Long id) {
        return ResponseEntity.ok(entMasWeberService.getById(id));
    }

    @PostMapping("entMasWeber/create")
    public ResponseEntity<ApiResponse<EntMasWeberResponse>> createEntMasWeber(
            @RequestBody @Valid EntMasWeberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entMasWeberService.create(request));
    }

    @PutMapping("entMasWeber/update/{id}")
    public ResponseEntity<ApiResponse<EntMasWeberResponse>> updateEntMasWeber(
            @PathVariable Long id,
            @RequestBody @Valid EntMasWeberRequest request) {
        return ResponseEntity.ok(entMasWeberService.update(id, request));
    }

    @PutMapping("entMasWeber/status/{id}")
    public ResponseEntity<ApiResponse<EntMasWeberResponse>> changeStatusEntMasWeber(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(entMasWeberService.changeStatus(id, status));
    }
    //    ===============================Ent Mas Wucosa ====================

    @GetMapping("entMasMucosa/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<EntMasMucosaResponse>>> getAllEntMasMucosa(
            @PathVariable int flag) {
        return ResponseEntity.ok(entMasMucosaService.getAll(flag));
    }

    @GetMapping("entMasMucosa/getById/{id}")
    public ResponseEntity<ApiResponse<EntMasMucosaResponse>> getByIdEntMasMucosa(
            @PathVariable Long id) {
        return ResponseEntity.ok(entMasMucosaService.getById(id));
    }

    @PostMapping("entMasMucosa/create")
    public ResponseEntity<ApiResponse<EntMasMucosaResponse>> createEntMasMucosa(
            @RequestBody @Valid EntMasMucosaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entMasMucosaService.create(request));
    }

    @PutMapping("entMasMucosa/update/{id}")
    public ResponseEntity<ApiResponse<EntMasMucosaResponse>> updateEntMasMucosa(
            @PathVariable Long id,
            @RequestBody @Valid EntMasMucosaRequest request) {
        return ResponseEntity.ok(entMasMucosaService.update(id, request));
    }

    @PutMapping("entMasMucosa/status/{id}")
    public ResponseEntity<ApiResponse<EntMasMucosaResponse>> changeStatusEntMasMucosa(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(entMasMucosaService.changeStatus(id, status));
    }


    //    ===============================Ent Mas Septum ====================


    @GetMapping("entMasSeptum/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<EntMasSeptumResponse>>> getAllEntMasSeptum(
            @PathVariable int flag) {
        return ResponseEntity.ok(entMasSeptumService.getAll(flag));
    }

    @GetMapping("entMasSeptum/getById/{id}")
    public ResponseEntity<ApiResponse<EntMasSeptumResponse>> getByIdEntMasSeptum(
            @PathVariable Long id) {
        return ResponseEntity.ok(entMasSeptumService.getById(id));
    }

    @PostMapping("entMasSeptum/create")
    public ResponseEntity<ApiResponse<EntMasSeptumResponse>> createEntMasSeptum(
            @RequestBody @Valid EntMasSeptumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entMasSeptumService.create(request));
    }

    @PutMapping("entMasSeptum/update/{id}")
    public ResponseEntity<ApiResponse<EntMasSeptumResponse>> updateEntMasSeptum(
            @PathVariable Long id,
            @RequestBody @Valid EntMasSeptumRequest request) {
        return ResponseEntity.ok(entMasSeptumService.update(id, request));
    }

    @PutMapping("entMasSeptum/status/{id}")
    public ResponseEntity<ApiResponse<EntMasSeptumResponse>> changeStatusEntMasSeptum(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(entMasSeptumService.changeStatus(id, status));
    }

    //    ===============================Ent Mas Tonsil Grade ====================

    @GetMapping("entMasTonsilGrade/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<EntMasTonsilGradeResponse>>> getAllEntMasTonsilGrade(
            @PathVariable int flag) {
        return ResponseEntity.ok(entMasTonsilGradeService.getAll(flag));
    }

    @GetMapping("entMasTonsilGrade/getById/{id}")
    public ResponseEntity<ApiResponse<EntMasTonsilGradeResponse>> getByIdEntMasTonsilGrade(
            @PathVariable Long id) {
        return ResponseEntity.ok(entMasTonsilGradeService.getById(id));
    }

    @PostMapping("entMasTonsilGrade/create")
    public ResponseEntity<ApiResponse<EntMasTonsilGradeResponse>> createEntMasTonsilGrade(
            @RequestBody @Valid EntMasTonsilGradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entMasTonsilGradeService.create(request));
    }

    @PutMapping("entMasTonsilGrade/update/{id}")
    public ResponseEntity<ApiResponse<EntMasTonsilGradeResponse>> updateEntMasTonsilGrade(
            @PathVariable Long id,
            @RequestBody @Valid EntMasTonsilGradeRequest request) {
        return ResponseEntity.ok(entMasTonsilGradeService.update(id, request));
    }

    @PutMapping("entMasTonsilGrade/status/{id}")
    public ResponseEntity<ApiResponse<EntMasTonsilGradeResponse>> changeStatusEntMasTonsilGrade(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(entMasTonsilGradeService.changeStatus(id, status));
    }


    //    ===============================Mas Vaccine ====================

    @GetMapping("masVaccine/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasVaccineMasterResponse>>> getAllVaccine(
            @PathVariable int flag) {
        return ResponseEntity.ok(masVaccineMasterService.getAll(flag));
    }

    @GetMapping("masVaccine/getById/{id}")
    public ResponseEntity<ApiResponse<MasVaccineMasterResponse>> getByIdVaccine(
            @PathVariable Long id) {
        return ResponseEntity.ok(masVaccineMasterService.getById(id));
    }

    @PostMapping("masVaccine/create")
    public ResponseEntity<ApiResponse<MasVaccineMasterResponse>> createVaccine(
            @RequestBody @Valid MasVaccineMasterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masVaccineMasterService.create(request));
    }

    @PutMapping("masVaccine/update/{id}")
    public ResponseEntity<ApiResponse<MasVaccineMasterResponse>> updateVaccine(
            @PathVariable Long id,
            @RequestBody @Valid MasVaccineMasterRequest request) {
        return ResponseEntity.ok(masVaccineMasterService.update(id, request));
    }

    @PutMapping("masVaccine/status/{id}")
    public ResponseEntity<ApiResponse<MasVaccineMasterResponse>> changeStatusVaccine(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masVaccineMasterService.changeStatus(id, status));
    }

    //    ===============================Mas Question heading ====================

    @GetMapping("masQuestionHeading/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasQuestionHeadingResponse>>> getAllMasQuestionHeading(
            @PathVariable int flag) {
        return ResponseEntity.ok(masQuestionHeadingService.getAll(flag));
    }

    @GetMapping("masQuestionHeading/getById/{id}")
    public ResponseEntity<ApiResponse<MasQuestionHeadingResponse>> getByIdMasQuestionHeading(
            @PathVariable Long id) {
        return ResponseEntity.ok(masQuestionHeadingService.getById(id));
    }

    @PostMapping("masQuestionHeading/create")
    public ResponseEntity<ApiResponse<MasQuestionHeadingResponse>> createMasQuestionHeading(
            @RequestBody @Valid MasQuestionHeadingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masQuestionHeadingService.create(request));
    }

    @PutMapping("masQuestionHeading/update/{id}")
    public ResponseEntity<ApiResponse<MasQuestionHeadingResponse>> updateMasQuestionHeading(
            @PathVariable Long id,
            @RequestBody @Valid MasQuestionHeadingRequest request) {
        return ResponseEntity.ok(masQuestionHeadingService.update(id, request));
    }

    @PutMapping("masQuestionHeading/status/{id}")
    public ResponseEntity<ApiResponse<MasQuestionHeadingResponse>> changeStatusMasQuestionHeading(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masQuestionHeadingService.changeStatus(id, status));
    }

    //    ===============================Mas Question====================

    @GetMapping("masQuestion/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasQuestionResponse>>> getAllMasQuestion(
            @PathVariable int flag) {
        return ResponseEntity.ok(questionService.getAll(flag));
    }

    @GetMapping("masQuestion/getById/{id}")
    public ResponseEntity<ApiResponse<MasQuestionResponse>> getByIdMasQuestion(
            @PathVariable Long id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    @PostMapping("masQuestion/create")
    public ResponseEntity<ApiResponse<MasQuestionResponse>> createMasQuestion(
            @RequestBody @Valid MasQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.create(request));
    }

    @PutMapping("masQuestion/update/{id}")
    public ResponseEntity<ApiResponse<MasQuestionResponse>> updateMasQuestion(
            @PathVariable Long id,
            @RequestBody @Valid MasQuestionRequest request) {
        return ResponseEntity.ok(questionService.update(id, request));
    }

    @PutMapping("masQuestion/status/{id}")
    public ResponseEntity<ApiResponse<MasQuestionResponse>> changeStatusMasQuestion(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(questionService.changeStatus(id, status));
    }

    //-------------------------------------------Mas Lab Amendment Type ------------------------------------------

    @PostMapping("/lab-amendment-type/create")
    public ResponseEntity<?> create(@Valid @RequestBody MasLabResultAmendmentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labResultAmendmentTypeService.create(request));
    }

    @PutMapping("/lab-amendment-type/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MasLabResultAmendmentTypeRequest request) {
        return ResponseEntity.ok(labResultAmendmentTypeService.update(id, request));
    }

    @PutMapping("/lab-amendment-type/{id}/status/{status}")
    public ResponseEntity<?> changeActiveStatus(@PathVariable Long id, @PathVariable String status) {
        return ResponseEntity.ok(labResultAmendmentTypeService.changeActiveStatus(id, status));
    }

    @GetMapping("/lab-amendment-type/{id}")
    public ResponseEntity<?> getAmendmentTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(labResultAmendmentTypeService.getById(id));
    }

    @GetMapping("/lab-amendment-type/all")
    public ResponseEntity<?> getAllAmendmentType(@RequestParam int flag) {
        return ResponseEntity.ok(labResultAmendmentTypeService.getAll(flag));
    }

    //-------------------------------------------Mas Patient Preparation------------------------------------------

    @PostMapping("/patient-preparation/create")
    public ResponseEntity<?> create(@Valid @RequestBody MasPatientPreparationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(masPatientPreparationService.create(request));
    }

    @PutMapping("/patient-preparation/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MasPatientPreparationRequest request) {
        return ResponseEntity.ok(masPatientPreparationService.update(id, request));
    }

    @PutMapping("/patient-preparation/{id}/status/{status}")
    public ResponseEntity<?> changeMasPatientPrepActiveStatus(@PathVariable Long id, @PathVariable String status) {
        return ResponseEntity.ok(masPatientPreparationService.changeActiveStatus(id, status));
    }

    @GetMapping("/patient-preparation/{id}")
    public ResponseEntity<?> getMasPatientPrepById(@PathVariable Long id) {
        return ResponseEntity.ok(masPatientPreparationService.getById(id));
    }

    @GetMapping("/patient-preparation/all")
    public ResponseEntity<?> getAllMasPatientPrep(@RequestParam int flag) {
        return ResponseEntity.ok(masPatientPreparationService.getAll(flag));
    }

    @GetMapping("/cancel-payment-reason/{flag}")
    public ResponseEntity<ApiResponse<List<MasAppointmentChangeReasonResponse>>> getAllReasons(
            @PathVariable int flag) {
        return ResponseEntity.ok(masAppointmentReasonService.getAllReasons(flag));
    }


    //-------------------------------------------Mas Blood Component------------------------------------------

    @GetMapping("masBloodComponent/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodComponentResponse>>> getAllBloodComponent(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodComponentService.getAll(flag));
    }

    @GetMapping("masBloodComponent/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodComponentResponse>> getByIdBloodComponent(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodComponentService.getById(id));
    }

    @PostMapping("masBloodComponent/create")
    public ResponseEntity<ApiResponse<MasBloodComponentResponse>> createBloodComponent(
            @RequestBody @Valid MasBloodComponentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodComponentService.create(request));
    }

    @PutMapping("masBloodComponent/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodComponentResponse>> updateBloodComponent(
            @PathVariable Long id,
            @RequestBody @Valid MasBloodComponentRequest request) {
        return ResponseEntity.ok(masBloodComponentService.update(id, request));
    }

    @PutMapping("masBloodComponent/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodComponentResponse>> changeStatusBloodComponent(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBloodComponentService.changeStatus(id, status));
    }

    //------------------------------------------- Mas Blood Donation Type------------------------------------------

    @GetMapping("masBloodDonationType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodDonationTypeResponse>>> getAllBloodDonationType(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodDonationTypeService.getAll(flag));
    }

    @GetMapping("masBloodDonationType/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodDonationTypeResponse>> getByIdBloodDonationType(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodDonationTypeService.getById(id));
    }

    @PostMapping("masBloodDonationType/create")
    public ResponseEntity<ApiResponse<MasBloodDonationTypeResponse>> createBloodDonationType(
            @RequestBody @Valid MasBloodDonationTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodDonationTypeService.create(request));
    }

    @PutMapping("masBloodDonationType/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodDonationTypeResponse>> updateBloodDonationType(
            @PathVariable Long id,
            @RequestBody @Valid MasBloodDonationTypeRequest request) {
        return ResponseEntity.ok(masBloodDonationTypeService.update(id, request));
    }

    @PutMapping("masBloodDonationType/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodDonationTypeResponse>> changeStatusBloodDonationType(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBloodDonationTypeService.changeStatus(id, status));
    }

    //------------------------------------------- Mas Blood Donation Status------------------------------------------
    @GetMapping("masBloodDonationStatus/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodDonationStatusResponse>>> getAllBloodDonationStatus(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodDonationStatusService.getAll(flag));
    }

    @GetMapping("masBloodDonationStatus/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodDonationStatusResponse>> getByIdBloodDonationStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodDonationStatusService.getById(id));
    }

    @PostMapping("masBloodDonationStatus/create")
    public ResponseEntity<ApiResponse<MasBloodDonationStatusResponse>> createBloodDonationStatus(
            @RequestBody @Valid MasBloodDonationStatusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodDonationStatusService.create(request));
    }

    @PutMapping("masBloodDonationStatus/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodDonationStatusResponse>> updateBloodDonationStatus(
            @PathVariable Long id,
            @RequestBody @Valid MasBloodDonationStatusRequest request) {
        return ResponseEntity.ok(masBloodDonationStatusService.update(id, request));
    }

    @PutMapping("masBloodDonationStatus/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodDonationStatusResponse>> changeStatusBloodDonationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBloodDonationStatusService.changeStatus(id, status));
    }

    //-------------------------------------------Mas Blood Bag Type---------------------------

    @GetMapping("masBloodBagType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodBagTypeResponse>>> getAllBloodBagType(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodBagTypeService.getAll(flag));
    }

    @GetMapping("masBloodBagType/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodBagTypeResponse>> getByIdBloodBagType(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodBagTypeService.getById(id));
    }

    @PostMapping("masBloodBagType/create")
    public ResponseEntity<ApiResponse<MasBloodBagTypeResponse>> createBloodBagType(
            @RequestBody @Valid MasBloodBagTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodBagTypeService.create(request));
    }

    @PutMapping("masBloodBagType/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodBagTypeResponse>> updateBloodBagType(
            @PathVariable Long id,
            @RequestBody @Valid MasBloodBagTypeRequest request) {
        return ResponseEntity.ok(masBloodBagTypeService.update(id, request));
    }

    @PutMapping("masBloodBagType/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodBagTypeResponse>> changeStatusBloodBagType(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBloodBagTypeService.changeStatus(id, status));
    }
    //-------------------------------------------Mas Blood Collection Type---------------------------

    @GetMapping("masBloodCollectionType/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodCollectionTypeResponse>>> getAllBloodCollectionType(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodCollectionTypeService.getAll(flag));
    }

    @GetMapping("masBloodCollectionType/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodCollectionTypeResponse>> getByIdBloodCollectionType(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodCollectionTypeService.getById(id));
    }

    @PostMapping("masBloodCollectionType/create")
    public ResponseEntity<ApiResponse<MasBloodCollectionTypeResponse>> createBloodCollectionType(
            @Valid @RequestBody MasBloodCollectionTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodCollectionTypeService.create(request));
    }

    @PutMapping("masBloodCollectionType/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodCollectionTypeResponse>> updateBloodCollectionType(
            @PathVariable Long id,
            @Valid @RequestBody MasBloodCollectionTypeRequest request) {
        return ResponseEntity.ok(masBloodCollectionTypeService.update(id, request));
    }

    @PutMapping("masBloodCollectionType/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodCollectionTypeResponse>> changeStatusBloodCollectionType(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBloodCollectionTypeService.changeStatus(id, status));
    }
    //-------------------------------------------Mas Blood Inventory Status---------------------------

    @GetMapping("masBloodInventoryStatus/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodInventoryStatusResponse>>> getAllBloodInventoryStatus(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodInventoryStatusService.getAll(flag));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodInventoryStatusResponse>> getByIdBloodInventoryStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodInventoryStatusService.getById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MasBloodInventoryStatusResponse>> createBloodInventoryStatus(
            @Valid @RequestBody MasBloodInventoryStatusRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodInventoryStatusService.create(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodInventoryStatusResponse>> updateBloodInventoryStatus(
            @PathVariable Long id,
            @Valid @RequestBody MasBloodInventoryStatusRequest request) {

        return ResponseEntity.ok(masBloodInventoryStatusService.update(id, request));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodInventoryStatusResponse>> changeStatusBloodInventoryStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(masBloodInventoryStatusService.changeStatus(id, status));
    }

    //------------------------------------------- Mas Blood Unit Status---------------------------
    @GetMapping("masBloodUnitStatus/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodUnitStatusResponse>>> getAllBloodUnitStatus(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodUnitStatusService.getAll(flag));
    }

    @GetMapping("masBloodUnitStatus/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodUnitStatusResponse>> getByIdBloodUnitStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodUnitStatusService.getById(id));
    }

    @PostMapping("masBloodUnitStatus/create")
    public ResponseEntity<ApiResponse<MasBloodUnitStatusResponse>> createBloodUnitStatus(
            @Valid @RequestBody MasBloodUnitStatusRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodUnitStatusService.create(request));
    }

    @PutMapping("masBloodUnitStatus/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodUnitStatusResponse>> updateBloodUnitStatus(
            @PathVariable Long id,
            @Valid @RequestBody MasBloodUnitStatusRequest request) {

        return ResponseEntity.ok(masBloodUnitStatusService.update(id, request));
    }

    @PutMapping("masBloodUnitStatus/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodUnitStatusResponse>> changeStatusBloodUnitStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(masBloodUnitStatusService.changeStatus(id, status));
    }


    //-------------------------------------------Mas Blood Test---------------------------
    @GetMapping("masBloodTest/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodTestResponse>>> getAllBloodTest(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodTestService.getAll(flag));
    }

    @GetMapping("masBloodTest/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodTestResponse>> getByIdBloodTest(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodTestService.getById(id));
    }

    @PostMapping("masBloodTest/create")
    public ResponseEntity<ApiResponse<MasBloodTestResponse>> createBloodTest(
            @Valid @RequestBody MasBloodTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodTestService.create(request));
    }

    @PutMapping("masBloodTest/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodTestResponse>> updateBloodTest(
            @PathVariable Long id,
            @Valid @RequestBody MasBloodTestRequest request) {
        return ResponseEntity.ok(masBloodTestService.update(id, request));
    }

    @PutMapping("masBloodTest/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodTestResponse>> changeStatusBloodTest(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBloodTestService.changeStatus(id, status));
    }

    //-------------------------------------------Mas Blood Compatibility---------------------------

    @GetMapping("masBloodCompatibility/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasBloodCompatibilityResponse>>> getAllMasBloodCompatibility(
            @PathVariable int flag) {
        return ResponseEntity.ok(masBloodCompatibilityService.getAll(flag));
    }

    @GetMapping("masBloodCompatibility/getById/{id}")
    public ResponseEntity<ApiResponse<MasBloodCompatibilityResponse>> getByIdMasBloodCompatibility(
            @PathVariable Long id) {
        return ResponseEntity.ok(masBloodCompatibilityService.getById(id));
    }

    @PostMapping("masBloodCompatibility/create")
    public ResponseEntity<ApiResponse<MasBloodCompatibilityResponse>> createMasBloodCompatibility(
            @Valid @RequestBody MasBloodCompatibilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masBloodCompatibilityService.create(request));
    }

    @PutMapping("masBloodCompatibility/update/{id}")
    public ResponseEntity<ApiResponse<MasBloodCompatibilityResponse>> updateMasBloodCompatibility(
            @PathVariable Long id,
            @Valid @RequestBody MasBloodCompatibilityRequest request) {
        return ResponseEntity.ok(masBloodCompatibilityService.update(id, request));
    }

    @PutMapping("masBloodCompatibility/status/{id}")
    public ResponseEntity<ApiResponse<MasBloodCompatibilityResponse>> changeStatusMasBloodCompatibility(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(masBloodCompatibilityService.changeStatus(id, status));
    }


    //=======================================Mas Common Status ====================================================

    @GetMapping("mas-common-status/all/")
    public ResponseEntity<?> getAllCommonStatus() {
        return ResponseEntity.ok(masCommonStatusService.getAllCommonStatus());
    }

    @GetMapping("mas-common-status/{id}")
    public ResponseEntity<?> getCommonStatusById(@PathVariable Long id) {
        return ResponseEntity.ok(masCommonStatusService.getCommonStatusById(id));
    }

    @PostMapping("mas-common-status/create")
    public ResponseEntity<?> createCommonStatus(@Valid @RequestBody MasCommonStatusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masCommonStatusService.createCommonStatus(request));
    }

    @PutMapping("mas-common-status/update/{id}")
    public ResponseEntity<?> updateCommonStatusById(@PathVariable Long id, @Valid @RequestBody MasCommonStatusRequest request) {
        return ResponseEntity.ok(masCommonStatusService.updateCommonStatusById(id, request));
    }

    @GetMapping("mas-common-status/search")
    public Page<EntityNameResponse> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return masCommonStatusService.searchEntities(keyword, PageRequest.of(page, size));
    }

    @GetMapping("mas-common-status/table")
    public ResponseEntity<?> getTableName(@RequestParam String entityName ) {
        return ResponseEntity.ok(masCommonStatusService.getTableNameForEntity(entityName));
    }

    @GetMapping("mas-common-status/columns")
    public ResponseEntity<?> getColumnNames(@RequestParam String entityName ) {
        return ResponseEntity.ok(masCommonStatusService.getColumnNamesFromEntity(entityName));
    }
    //=======================================Mas Language ====================================================
    @GetMapping("masLanguage/getAll/{flag}")
    public ResponseEntity<ApiResponse<List<MasLanguageResponse>>> getAllMasLanguage(
            @PathVariable int flag) {
        return ResponseEntity.ok(masLanguageService.getAll(flag));
    }

}