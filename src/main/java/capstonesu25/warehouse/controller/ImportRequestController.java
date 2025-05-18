package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.importrequest.ImportRequestCreateRequest;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ImportRequestService;
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

@Controller
@RequestMapping("/import-request")
@RequiredArgsConstructor
@Validated
public class ImportRequestController {
    private final ImportRequestService importRequestService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestController.class);

    @Operation(summary = "Get all import requests")
    @GetMapping()
    public ResponseEntity<?> getAll() {
        LOGGER.info("Getting all import requests");
        return ResponseUtil.getCollection(
                importRequestService.getAllImportRequests(),
                HttpStatus.OK,
                "Successfully retrieved all import requests",
                null);
    }

    @Operation(summary = "Get paginated import requests")
    @GetMapping("/page")
    public ResponseEntity<?> getAllByPage(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all import requests by page");
        Page<ImportRequestResponse> result = importRequestService.getAllImportRequestsByPage(page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get paginated import requests",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));

    }

    @Operation(summary = "Get import request by import request Id")
    @GetMapping("/{importRequestId}")
    public ResponseEntity<?> getById(@PathVariable String importRequestId) {
        LOGGER.info("Getting import request by id");
        ImportRequestResponse result = importRequestService.getImportRequestById(importRequestId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved import request");
    }

    @Operation(summary = "Create a new import request")
    @PostMapping()
    public ResponseEntity<?> createImportRequest(@RequestBody ImportRequestCreateRequest request) {
        LOGGER.info("Creating import request");
        return ResponseUtil.getObject(
                importRequestService.createImportRequest(request),
                HttpStatus.CREATED,
                "Successfully created import request");
    }
}
