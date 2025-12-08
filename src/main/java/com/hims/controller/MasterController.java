package com.hims.controller;

import com.hims.entity.*;
import com.hims.entity.repository.MasMedicalHistoryRepository;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.*;
import com.hims.service.impl.UserDepartmentServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "MasterController", description = "Controller for handling All Master")
@RequestMapping("/master")
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
    public ApiResponse<List<MasMedicalHistoryResponse>> getmasMedicalHistory(@PathVariable int flag) {
        return masMedicalHistoryService.getAllMas(flag);
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



}