package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.annotation.transactionLog.TransactionLoggable;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailRequest;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailResponse;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailUpdateRequest;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ImportOrderDetailService;
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
@RequestMapping("/import-order-detail")
@RequiredArgsConstructor
@Validated
public class ImportOrderDetailController {
    private final ImportOrderDetailService service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderDetailController.class);

    @Operation(summary = "Get paginated import order details by import order ID")
    @GetMapping("/page/{importOrderId}")

    public ResponseEntity<?> getImportOrderDetails(@PathVariable String importOrderId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting import order details");
        Page<ImportOrderDetailResponse> result = service.getAllByImportOrderId(importOrderId, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get paginated import order details by import order ID",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get import order detail by ID")
    @GetMapping("/{importOrderDetailId}")

    public ResponseEntity<?> getImportOrderDetail(@PathVariable Long importOrderDetailId) {
        LOGGER.info("Getting import order detail");
        var result = service.getById(importOrderDetailId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully get import order detail");
    }

    @Operation(summary = "Create import order details from Excel file")
    @PostMapping("/{importOrderId}")
    public ResponseEntity<?> createImportOrderDetails(@RequestBody ImportOrderDetailRequest request,
            @PathVariable String importOrderId) {
        LOGGER.info("Creating import order details from Excel file");
        return ResponseUtil.getObject(
                service.create(request, importOrderId),
                HttpStatus.CREATED,
                "Successfully created import order details");
    }

    @Operation(summary = "Update actual quantities of import order details")
    @PutMapping("/{importOrderId}")
    public ResponseEntity<?> updateActualQuantities(@RequestBody List<ImportOrderDetailUpdateRequest> requests,
            @PathVariable String importOrderId) {
        LOGGER.info("Updating actual quantities of import order details");
        service.updateActualQuantities(requests, importOrderId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated actual quantities");
    }

    @Operation(summary = "Delete import order detail by ID")
    @DeleteMapping("/{importOrderDetailId}")
    public ResponseEntity<?> deleteImportOrderDetail(@PathVariable Long importOrderDetailId) {
        LOGGER.info("Deleting import order detail");
        service.delete(importOrderDetailId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted import order detail");
    }
}
