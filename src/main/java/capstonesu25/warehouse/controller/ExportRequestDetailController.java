package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestActualQuantity;
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
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/export-request-detail")
@RequiredArgsConstructor
@Validated
public class ExportRequestDetailController {
    private final ExportRequestDetailService service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestDetailController.class);

    @Operation(summary = "Create export request details from file upload")
    @PostMapping("/{exportRequestId}")
    public ResponseEntity<?> createExportRequestDetail(@RequestPart MultipartFile file, @PathVariable Long exportRequestId) {
        LOGGER.info("Creating export request detail");
        service.createExportRequestDetail(file, exportRequestId);
        return ResponseUtil.getObject(
            null,
            HttpStatus.CREATED,
            "Successfully created export request details"
        );
    }

    @Operation(summary = "Update actual quantity of export request detail")
    @PutMapping("/actual-quantity")
    public ResponseEntity<?> updateActualQuantity(@RequestBody ExportRequestActualQuantity actualQuantity) {
        LOGGER.info("Updating actual quantity for export request detail with ID: {}", actualQuantity.getExportRequestDetailId());
        service.updateActualQuantity(actualQuantity.getExportRequestDetailId(), actualQuantity.getActualQuantity());
        return ResponseUtil.getObject(
            null,
            HttpStatus.OK,
            "Successfully updated actual quantity"
        );
    }
} 