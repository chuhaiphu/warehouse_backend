package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestCreateWithDetailRequest;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ImportRequestDetailService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/import-request-detail")
@RequiredArgsConstructor
@Validated
public class ImportRequestDetailController {
    private final ImportRequestDetailService service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestDetailController.class);

    @Operation(summary = "Get paginated import request details by import request ID")
    @GetMapping("/import-request/{importRequestId}")
    public ResponseEntity<?> getImportRequestDetails(@PathVariable String importRequestId) {
        LOGGER.info("Getting import request detail");
        List<ImportRequestDetailResponse> result = service.getImportRequestDetailsByImportRequestId(importRequestId);

        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully get import request detail",
                null);
    }

    @Operation(summary = "Get import request detail by ID")
    @GetMapping("/{importRequestDetailId}")
    public ResponseEntity<?> getImportRequestDetail(@PathVariable Long importRequestDetailId) {
        LOGGER.info("Getting import request detail");
        return ResponseUtil.getObject(
                service.getImportRequestDetailById(importRequestDetailId),
                HttpStatus.OK,
                "Successfully get import request detail");
    }

    @Operation(summary = "Delete import request details by import request ID")
    @DeleteMapping("/{importRequestDetailId}")
    public ResponseEntity<?> deleteImportRequestDetail(@PathVariable Long importRequestDetailId) {
        LOGGER.info("Deleting import request detail");
        service.deleteImportRequestDetail(importRequestDetailId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted import request");
    }

    @Operation(summary = "Create import requests with details")
    @PostMapping("/import-requests-with-import-request-details")
    public ResponseEntity<?> createImportRequestsWithDetails(@RequestBody List<ImportRequestCreateWithDetailRequest> request) {
        LOGGER.info("Creating import requests with details");
        List<String> createdIds = service.createImportRequestDetail(request);
        return ResponseUtil.getObject(
                createdIds,
                HttpStatus.CREATED,
                "Successfully created import requests with details");
    }
}
