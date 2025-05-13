package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.exportrequest.UpdateExportDateTimeRequest;
import capstonesu25.warehouse.model.exportrequest.exportborrowing.ExportBorrowingRequest;
import capstonesu25.warehouse.model.exportrequest.exportliquidation.ExportLiquidationRequest;
import capstonesu25.warehouse.model.exportrequest.exportpartial.ExportPartialRequest;
import capstonesu25.warehouse.model.exportrequest.exportproduction.ExportRequestRequest;
import capstonesu25.warehouse.model.exportrequest.ExportRequestResponse;
import capstonesu25.warehouse.model.exportrequest.exportreturn.ExportReturnRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.importrequest.AssignStaffExportRequest;
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

    @Operation(summary = "Get list export request by staff Id")
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getByStaffId(@PathVariable Long staffId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting export request by staff id");
        Page<ExportRequestResponse> result
                = exportRequestService.getAllExportRequestByAssignStaff(staffId, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get paginated export requests by staff id",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page
                )
        );
    }

    @Operation(summary = "Create a new export request for production")
    @PostMapping()
    public ResponseEntity<?> createExportRequest(@RequestBody ExportRequestRequest request) {
        LOGGER.info("Creating export request");
        return ResponseUtil.getObject(
            exportRequestService.createExportProductionRequest(request),
            HttpStatus.CREATED,
            "Successfully created export request"
        );
    }

    @Operation(summary = "Create a new export request for return")
    @PostMapping("/return")
    public ResponseEntity<?> createExportRequestForReturn(@RequestBody ExportReturnRequest request) {
        LOGGER.info("Creating export request for return");
        return ResponseUtil.getObject(
            exportRequestService.createExportReturnRequest(request),
            HttpStatus.CREATED,
            "Successfully created export request for return"
        );
    }

    @Operation(summary = "Create a new export request for borrowing")
    @PostMapping("/borrow")
    public ResponseEntity<?> createExportRequestForBorrow(@RequestBody ExportBorrowingRequest request) {
        LOGGER.info("Creating export request for borrowing");
        return ResponseUtil.getObject(
            exportRequestService.createExportBorrowingRequest(request),
            HttpStatus.CREATED,
            "Successfully created export request for borrowing"
        );
    }

    @Operation(summary = "Create a new export request for liquidation")
    @PostMapping("/liquidation")
    public ResponseEntity<?> createExportRequestForLiquidation(@RequestBody ExportLiquidationRequest request) {
        LOGGER.info("Creating export request for liquidation");
        return ResponseUtil.getObject(
            exportRequestService.createExportLiquidationRequest(request),
            HttpStatus.CREATED,
            "Successfully created export request for liquidation"
        );
    }

    @Operation(summary = "Create a new export request for partial")
    @PostMapping("/partial")
    public ResponseEntity<?> createExportRequestForPartial(@RequestBody ExportPartialRequest request) {
        LOGGER.info("Creating export request for partial");
        return ResponseUtil.getObject(
            exportRequestService.createExportPartialRequest(request),
            HttpStatus.CREATED,
            "Successfully created export request for partial"
        );
    }


    @Operation(summary = "Assign warehouse keeper for confirmation of export request")
    @PostMapping("/assign-warehouse-keeper")
    public ResponseEntity<?> assignKeeperForConfirm(
        @RequestBody AssignStaffExportRequest request
    ) {
        LOGGER.info("Assigning warehouse keeper to export request");
        return ResponseUtil.getObject(
            exportRequestService.assignStaffToExportRequest(request),
            HttpStatus.OK,
            "Successfully updated export request"
        );
    }

    @Operation(summary = "Assign warehouse keeper for counting of export request")
    @PostMapping("/counting/assign-warehouse-keeper")
    public ResponseEntity<?> assignKeeperForCounting(
            @RequestBody AssignStaffExportRequest request
    ) {
        LOGGER.info("Assigning warehouse keeper to export request");
        return ResponseUtil.getObject(
                exportRequestService.assignCountingStaff(request),
                HttpStatus.OK,
                "Successfully updated export request"
        );
    }

    @Operation(summary = "Confirm counted export request")
    @PostMapping("/confirm-counted/{exportRequestId}")
    public ResponseEntity<?> confirmExportRequest(@PathVariable Long exportRequestId) {
        LOGGER.info(" Confirming export request");
        ExportRequestResponse result = exportRequestService.confirmCountedExportRequest(exportRequestId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully confirmed counted export request");
    }

    @Operation(summary = "complete export request")
    @PostMapping("/complete/{exportRequestId}")
    public ResponseEntity<?> completeExportRequest(@PathVariable Long exportRequestId) {
        LOGGER.info("Completing export request");
        ExportRequestResponse result = exportRequestService.completeExportRequest(exportRequestId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully completed export request");
    }

    @Operation(summary = "update export date and time for export request")
    @PostMapping("/update-export-date-time/{exportRequestId}")
    public ResponseEntity<?> updateExportDateTime(@PathVariable Long exportRequestId,
                                                  @RequestBody UpdateExportDateTimeRequest request) {
        LOGGER.info("Updating export date and time");
        return ResponseUtil.getObject(
                exportRequestService.updateExportDateTime(exportRequestId, request.getExportDate(),request.getExportTime()),
                HttpStatus.OK,
                "Successfully updated export date and time"
        );
    }

} 