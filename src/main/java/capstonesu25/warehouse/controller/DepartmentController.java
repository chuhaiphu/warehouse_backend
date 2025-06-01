package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.department.DepartmentResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.DepartmentService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/department")
@RequiredArgsConstructor
@Validated
public class DepartmentController {
    private final DepartmentService departmentService;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DepartmentController.class);

    @Operation(summary = "Get all departments with pagination")
    @GetMapping()
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int limit) {
        logger.info("Getting all departments with pagination");
        Page<DepartmentResponse> result = departmentService.getAllDepartments(page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get all departments with pagination",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get department by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        logger.info("Getting department by ID: {}", id);
        DepartmentResponse result = departmentService.getDepartmentById(id);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved department");
    }
}
