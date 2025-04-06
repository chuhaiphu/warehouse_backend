package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.importorder.AssignWarehouseKeeperRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
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
    public ResponseEntity<?> getAll(@PathVariable Long importRequestId, @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int limit){
        LOGGER.info("Getting all import orders");
        Page<ImportOrderResponse> result =  importOrderService.getImportOrdersByImportRequestId(importRequestId, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get import request detail",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page
                )
        );
    }

    @Operation(summary = "Get import order by ID")
    @GetMapping("/{importOrderId}")
    public ResponseEntity<?> getById(@PathVariable Long importOrderId){
        LOGGER.info("Getting import order by id");
        ImportOrderResponse result = importOrderService.getImportOrderById(importOrderId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved import order"
        );
    }

    @Operation(summary = "Create a new import order")
    @PostMapping()
    public ResponseEntity<?> createImportOrder(@RequestBody ImportOrderRequest request){
        LOGGER.info("Creating import order");
        return ResponseUtil.getObject(
                importOrderService.save(request),
                HttpStatus.CREATED,
                "Successfully created import order"
        );
    }

    @Operation(summary = "Update an existing import order")
    @PutMapping()
    public ResponseEntity<?> updateImportOrder(@RequestBody ImportOrderRequest request){
        LOGGER.info("Updating import order");
        return ResponseUtil.getObject(
                importOrderService.save(request),
                HttpStatus.OK,
                "Successfully updated import order"
        );
    }

    @Operation(summary = "Delete an import order by ID")
    @DeleteMapping("/{importOrderId}")
    public ResponseEntity<?> deleteImportOrder(@PathVariable Long importOrderId){
        LOGGER.info("Deleting import order");
        importOrderService.delete(importOrderId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted import order"
        );
    }

    @Operation(summary = "Assign warehouse keeper to an import order")
    @PostMapping("/assign-warehouse-keeper")
    public ResponseEntity<?> assignWarehouseKeeper(@RequestBody AssignWarehouseKeeperRequest request) {
        LOGGER.info("Assigning warehouse keeper to import order");
        ImportOrderResponse result = importOrderService.assignWarehouseKeeper(
                request.getImportOrderId(), 
                request.getAccountId());
        
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully assigned warehouse keeper to import order"
        );
    }
}
