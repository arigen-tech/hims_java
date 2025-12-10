package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.request.IssueInternalIndentApprovalRequest;
import com.hims.request.StoreInternalIndentApprovalRequest;
import com.hims.request.StoreInternalIndentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ROLItemResponse;
import com.hims.response.StoreInternalIndentResponse;
import com.hims.service.StoreInternalIndentService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storeInternalIndent")
@RequiredArgsConstructor

public class StoreInternalIndentController {

    private final StoreInternalIndentService indentService;

    /**
     * Save (create or update) indent as DRAFT - backend sets status "S"
     */
    @PostMapping("/save")
    public ApiResponse<StoreInternalIndentResponse> saveIndent(@RequestBody StoreInternalIndentRequest request) {
        return indentService.saveIndent(request);
    }

    /**
     * Submit an existing indent - backend sets status "Y"
     */
    @PostMapping("/submit")
    public ApiResponse<StoreInternalIndentResponse> submitIndent(@RequestBody StoreInternalIndentRequest request) {
        return indentService.submitIndent(request);
    }

    /**
     * Get indent by id (with details)
     */
    @GetMapping("/{indentMId}")
    public ApiResponse<StoreInternalIndentResponse> getById(@PathVariable Long indentMId) {
        return indentService.getIndentById(indentMId);
    }

    /**
     * List indents of current department. Optional status filter ("S" or "Y")
     */
    @GetMapping("/getallindent")
    public ApiResponse<List<StoreInternalIndentResponse>> list() {
        return indentService.listIndentsByCurrentDept();
    }


    /**
     * Create indent from ROL (body contains items or backend can compute them)
     */
    @PostMapping("/fromRol")
    public ApiResponse<StoreInternalIndentResponse> fromRol(@RequestBody StoreInternalIndentRequest request) {
        return indentService.createIndentFromROL(request);
    }

    /**
     * Create indent from previous indent
     */
    @PostMapping("/fromPrevious/{previousIndentMId}")
    public ApiResponse<StoreInternalIndentResponse> fromPrevious(@PathVariable Long previousIndentMId) {
        return indentService.createIndentFromPrevious(previousIndentMId);
    }

    @GetMapping("/fixed-dropdown")
    public ApiResponse<List<MasDepartment>> getFixedDeptDropdown() {
        List<MasDepartment> list = indentService.getOtherFixedDepartmentsForCurrentUser();
        return ResponseUtils.createSuccessResponse(
                list,
                new TypeReference<List<MasDepartment>>() {}
        );
    }

    @GetMapping("/rol-items")
    public ApiResponse<List<ROLItemResponse>> getROLItems() {
        return indentService.getROLItems();
    }



    @PostMapping("/approve")
    public ApiResponse<StoreInternalIndentResponse> approveRejectIndent(@RequestBody StoreInternalIndentApprovalRequest request) {
        return indentService.approveRejectIndent(request);
    }

    @GetMapping("/getallindentforpending")
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForPending(
            @RequestParam("deptId") Long deptId) {
        return indentService.getAllIndentsForPending(deptId);
    }

    @GetMapping("/getallindentforapproved")
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForApproved(
            @RequestParam("deptId") Long deptId) {
        return indentService.getAllIndentsForApproved(deptId);
    }



    @PostMapping("/submitapprove")
    public ApiResponse<StoreInternalIndentResponse> approveIndent(
            @RequestBody IssueInternalIndentApprovalRequest request) {
        return indentService.submitApprovedIndent(request);
    }

    @GetMapping("/getallindentforissue")
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForIssueDepartment(
            @RequestParam("deptId") Long deptId) {

        return indentService.getAllIndentsForIssueDepartment(deptId);
    }



}
