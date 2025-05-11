package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.importorder.AssignStaffRequest;

import capstonesu25.warehouse.model.importorder.ImportOrderCreateRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.importorder.ImportOrderUpdateRequest;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ImportOrderService;
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
@RequestMapping("/import-order")
@RequiredArgsConstructor
@Validated
public class ImportOrderController {
    private final ImportOrderService importOrderService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderController.class);

    @Operation(summary = "Get all import orders for a specific import request")
    @GetMapping("/page/{importRequestId}")
    public ResponseEntity<?> getAllImportOrdersByImportRequestId(@PathVariable Long importRequestId, @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all import orders");
        Page<ImportOrderResponse> result = importOrderService.getImportOrdersByImportRequestId(importRequestId, page,
                limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get import orders",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get import order by ID")
    @GetMapping("/{importOrderId}")
    public ResponseEntity<?> getById(@PathVariable Long importOrderId) {
        LOGGER.info("Getting import order by id");
        ImportOrderResponse result = importOrderService.getImportOrderById(importOrderId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved import order");
    }

    @Operation(summary = "Create a new import order")
    @PostMapping()
    public ResponseEntity<?> createImportOrder(@RequestBody ImportOrderCreateRequest request) {
        LOGGER.info("Creating import order");
        return ResponseUtil.getObject(
                importOrderService.create(request),
                HttpStatus.CREATED,
                "Successfully created import order");
    }

    @Operation(summary = "Update an existing import order")
    @PutMapping()
    public ResponseEntity<?> updateImportOrder(@RequestBody ImportOrderUpdateRequest request) {
        LOGGER.info("Updating import order");
        return ResponseUtil.getObject(
                importOrderService.update(request),
                HttpStatus.OK,
                "Successfully updated import order");
    }

    @Operation(summary = "Delete an import order by ID")
    @DeleteMapping("/{importOrderId}")
    public ResponseEntity<?> deleteImportOrder(@PathVariable Long importOrderId) {
        LOGGER.info("Deleting import order");
        importOrderService.delete(importOrderId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted import order");
    }

    @Operation(summary = "Assign staff to an import order")
    @PostMapping("/assign-staff")
    public ResponseEntity<?> assignStaff(@RequestBody AssignStaffRequest request) {
        LOGGER.info("Assigning staff to import order");
        ImportOrderResponse result = importOrderService.assignStaff(
                request.getImportOrderId(),
                request.getAccountId());

        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully assigned staff to import order");
    }

    @Operation(summary = "Get import orders by staff ID")
    @GetMapping("/staff/{staffId}")
    
    public ResponseEntity<?> getByStaffId(@PathVariable Long staffId, @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting import orders by staff id");
        Page<ImportOrderResponse> result = importOrderService.getImportOrdersByStaffId(staffId, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get import orders by staff",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get all import orders")
    @GetMapping("/page")
    public ResponseEntity<?> getAllImportOrders(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all import orders");
        Page<ImportOrderResponse> result = importOrderService.getAllImportOrders(page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get all import orders",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Cancel an import order")
    @PostMapping("/cancel/{importOrderId}")
    public ResponseEntity<?> cancelImportOrder(@PathVariable Long importOrderId) {
        LOGGER.info("Cancelling import order");
        ImportOrderResponse result = importOrderService.cancelImportOrder(importOrderId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully cancelled import order");
    }

    @Operation(summary = "complete an import order")
    @PostMapping("/complete/{importOrderId}")
    public ResponseEntity<?> completeImportOrder(@PathVariable Long importOrderId) {
        LOGGER.info("Completing import order");
        ImportOrderResponse result = importOrderService.completeImportOrder(importOrderId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully completed import order");
    }
}
