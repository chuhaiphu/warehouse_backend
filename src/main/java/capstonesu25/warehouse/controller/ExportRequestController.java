package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.exportrequest.ExportRequestRequest;
import capstonesu25.warehouse.model.exportrequest.ExportRequestResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ExportRequestService;
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
@RequestMapping("/export-request")
@RequiredArgsConstructor
@Validated
public class ExportRequestController {
    private final ExportRequestService exportRequestService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestController.class);

    @Operation(summary = "Get all export requests")
    @GetMapping()
    public ResponseEntity<?> getAll() {
        LOGGER.info("Getting all export requests");
        return ResponseUtil.getCollection(
            exportRequestService.getAllExportRequests(),
            HttpStatus.OK,
            "Successfully retrieved all export requests",
            null
        );
    }

    @Operation(summary = "Get paginated export requests")
    @GetMapping("/page")
    public ResponseEntity<?> getAllByPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit
    ) {
        LOGGER.info("Getting all export requests by page");
        Page<ExportRequestResponse> result = exportRequestService.getAllExportRequestsByPage(page, limit);
        return ResponseUtil.getCollection(
            result.getContent(),
            HttpStatus.OK,
            "Successfully get paginated export requests",
            new MetaDataDTO(
                result.hasNext(),
                result.hasPrevious(),
                limit,
                (int) result.getTotalElements(),
                page
            )
        );
    }

    @Operation(summary = "Get export request by export request Id")
    @GetMapping("/{exportRequestId}")
    public ResponseEntity<?> getById(@PathVariable Long exportRequestId) {
        LOGGER.info("Getting export request by id");
        ExportRequestResponse result = exportRequestService.getExportRequestById(exportRequestId);
        return ResponseUtil.getObject(
            result,
            HttpStatus.OK,
            "Successfully retrieved export request"
        );
    }

    @Operation(summary = "Create a new export request")
    @PostMapping()
    public ResponseEntity<?> createExportRequest(@RequestBody ExportRequestRequest request) {
        LOGGER.info("Creating export request");
        return ResponseUtil.getObject(
            exportRequestService.createExportRequest(request),
            HttpStatus.CREATED,
            "Successfully created export request"
        );
    }
} 