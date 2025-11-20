package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.request.StoreInternalIndentRequest;
import com.hims.response.ApiResponse;
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
    @PostMapping("/submit/{indentMId}")
    public ApiResponse<StoreInternalIndentResponse> submitIndent(@PathVariable Long indentMId) {
        return indentService.submitIndent(indentMId);
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
    @GetMapping("/list")
    public ApiResponse<List<StoreInternalIndentResponse>> list(@RequestParam(value = "status", required = false) String status) {
        return indentService.listIndentsByCurrentDept(status);
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

}
