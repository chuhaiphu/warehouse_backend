package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.stockcheck.AssignStaffStockCheck;
import capstonesu25.warehouse.model.stockcheck.CompleteStockCheckRequest;
import capstonesu25.warehouse.model.stockcheck.StockCheckRequestRequest;
import capstonesu25.warehouse.service.StockCheckService;
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

@Controller
@RequestMapping("/stock-check")
@RequiredArgsConstructor
@Validated
public class StockCheckController {
    private final StockCheckService stockCheckService;
    private static final Logger LOGGER = LoggerFactory.getLogger(StockCheckController.class);
    @Operation(summary = "Get all stock check requests")
    @GetMapping()
    public ResponseEntity<?> getAll() {
        LOGGER.info("Getting all  stock check requests");
        return ResponseUtil.getCollection(
                stockCheckService.getAllStockCheckRequests(),
                HttpStatus.OK,
                "Successfully retrieved all export requests",
                null
        );
    }

    @Operation(summary = "Get stock check request by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        LOGGER.info("Getting stock check request by id: {}", id);
        return ResponseUtil.getObject(
                stockCheckService.getStockCheckRequestById(id),
                HttpStatus.OK,
                "Successfully retrieved stock check request"
        );
    }

    @Operation(summary = "Get stock check requests by staff ID")
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getByStaffId(@PathVariable Long staffId) {
        LOGGER.info("Getting stock check requests by staff id: {}", staffId);
        return ResponseUtil.getCollection(
                stockCheckService.getAllStockCheckRequestsByStaffId(staffId),
                HttpStatus.OK,
                "Successfully retrieved stock check requests by staff id",
                null
        );
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new stock check request")
    public ResponseEntity<?> createStockCheckRequest(@RequestBody StockCheckRequestRequest request) {
        LOGGER.info("Creating stock check request with data: {}", request);
        return ResponseUtil.getObject(
                stockCheckService.createStockCheckRequest(request),
                HttpStatus.CREATED,
                "Successfully created stock check request"
        );
    }

    //assign new staff
    @Operation(summary = "Assign a staff member to a stock check request")
    @PostMapping("/assign-staff")
    public ResponseEntity<?> assignStaffToStockCheck(@RequestBody AssignStaffStockCheck request) {
        LOGGER.info("Assigning staff to stock check request with data: {}", request);
        return ResponseUtil.getObject(
                stockCheckService.assignStaffToStockCheck(request),
                HttpStatus.OK,
                "Successfully assigned staff to stock check request"
        );
    }

    // confirm counted

    @Operation(summary = "Confirm counted stock check request")
    @PutMapping("/confirm-counted/{stockCheckId}")
    public ResponseEntity<?> confirmCountedStockCheck(@PathVariable String stockCheckId) {
        LOGGER.info("Confirming counted stock check request with ID: {}", stockCheckId);
        return ResponseUtil.getObject(
                stockCheckService.confirmCountedStockCheck(stockCheckId),
                HttpStatus.OK,
                "Successfully confirmed counted stock check request"
        );
    }

    // complete
    @Operation(summary = "Complete stock check request")
    @PutMapping("/complete")
    public ResponseEntity<?> completeStockCheck(@RequestBody CompleteStockCheckRequest request) {
        LOGGER.info("Completing stock check request with ID: {}", request);
        return ResponseUtil.getObject(
                stockCheckService.completeStockCheck(request),
                HttpStatus.OK,
                "Successfully completed stock check request"
        );
    }

    // update status
    @PostMapping("update-status/{stockCheckId}")
    @Operation(summary = "Update status of stock check request")
    public ResponseEntity<?> updateStatus(@PathVariable String stockCheckId, @RequestParam RequestStatus status) {
        LOGGER.info("Updating status of stock check request with ID: {}", stockCheckId);
        return ResponseUtil.getObject(
                stockCheckService.updateStatus(stockCheckId,status),
                HttpStatus.OK,
                "Successfully updated status of stock check request"
        );
    }

}
