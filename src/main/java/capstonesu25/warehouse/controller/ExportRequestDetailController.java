package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestActualQuantity;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailRequest;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ExportRequestDetailService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Controller
@RequestMapping("/export-request-detail")
@RequiredArgsConstructor
@Validated
public class ExportRequestDetailController {
    private final ExportRequestDetailService service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestDetailController.class);

    @Operation(summary = "Get all export request details by export request ID")
    @GetMapping("/{exportRequestId}")
    public ResponseEntity<?> getAllByExportRequestId(@PathVariable String  exportRequestId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all export request details by export request ID: {}", exportRequestId);
        Page<ExportRequestDetailResponse> result = service.getAllByExportRequestId(exportRequestId, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get paginated export request details by export request ID",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get export request detail by ID")
    @GetMapping("/detail/{exportRequestDetailId}")
    public ResponseEntity<?> getExportRequestDetail(@PathVariable Long exportRequestDetailId) {
        LOGGER.info("Getting export request detail by ID: {}", exportRequestDetailId);
        var result = service.getById(exportRequestDetailId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully get export request detail"
        );
    }

    @Operation(summary = "Create export request details from file upload")
    @PostMapping("/{exportRequestId}")
    public ResponseEntity<?> createExportRequestDetail(@RequestBody List<ExportRequestDetailRequest> request,
                                                       @PathVariable String exportRequestId) {
        LOGGER.info("Creating export request detail");
        service.createExportRequestDetail(request, exportRequestId);
        return ResponseUtil.getObject(
            null,
            HttpStatus.CREATED,
            "Successfully created export request details"
        );
    }

    @Operation(summary = "Update actual quantity of export request detail")
    @PutMapping("/actual-quantity")
    public ResponseEntity<?> updateActualQuantity(@RequestBody ExportRequestActualQuantity request) {
        LOGGER.info("Updating actual quantity for export request detail with ID: {}", request.getExportRequestDetailId());
        return ResponseUtil.getObject(
                service.updateActualQuantity(
                        request.getExportRequestDetailId(), request.getActualQuantity()),
            HttpStatus.OK,
            "Successfully updated actual quantity"
        );
    }
} 