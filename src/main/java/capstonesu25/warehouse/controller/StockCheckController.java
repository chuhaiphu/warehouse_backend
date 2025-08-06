package capstonesu25.warehouse.controller;

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
    // confirm counted
    // complete
    // update status

}
