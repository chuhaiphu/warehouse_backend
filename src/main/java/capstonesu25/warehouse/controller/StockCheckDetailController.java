package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.stockcheck.StockCheckRequestRequest;
import capstonesu25.warehouse.model.stockcheck.detail.StockCheckRequestDetailRequest;
import capstonesu25.warehouse.model.stockcheck.detail.UpdateActualStockCheck;
import capstonesu25.warehouse.service.StockCheckDetailService;
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

import java.util.List;

@Controller
@RequestMapping("/stock-check-detail")
@RequiredArgsConstructor
@Validated
public class StockCheckDetailController {
    private final StockCheckDetailService stockCheckDetailService;
    private static final Logger LOGGER = LoggerFactory.getLogger(StockCheckDetailController.class);

    @Operation(summary = "Get all stock check request details by stock check id")
    @GetMapping("/{stockCheckId}")
    public ResponseEntity<?> getAll(@PathVariable String stockCheckId) {
        LOGGER.info("Getting all stock check request details by stock check id");
        return ResponseUtil.getCollection(
                stockCheckDetailService.getAllByStockCheckRequestId(stockCheckId),
                HttpStatus.OK,
                "Successfully retrieved all export requests",
                null
        );
    }

    @Operation(summary = "Get stock check request detail by ID")
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        LOGGER.info("Getting stock check request detail by id: {}", id);
        return ResponseUtil.getObject(
                stockCheckDetailService.getById(id),
                HttpStatus.OK,
                "Successfully retrieved stock check request"
        );
    }

    @PostMapping("/{stockCheckId}")
    @Operation(summary = "Create a new stock check request detail")
    public ResponseEntity<?> createStockCheckRequest(@RequestBody List<StockCheckRequestDetailRequest> request,
                                                     @PathVariable String stockCheckId) {
        LOGGER.info("Creating stock check request with data: {}", request);
        stockCheckDetailService.create(request, stockCheckId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created stock check request"
        );
    }

    @Operation(summary = "Update stock check request detail by ID")
    @PutMapping("/tracking")
    public ResponseEntity<?> updateStockCheckRequestDetail(@RequestBody UpdateActualStockCheck request) {
        LOGGER.info("Updating stock check request detail with data: {}", request);
        stockCheckDetailService.updateActualQuantity(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated stock check request detail"
        );
    }

    @Operation(summary = "reset tracking for stock check request detail by ID")
    @PutMapping("/reset-tracking")
    public ResponseEntity<?> resetTrackingForStockCheckRequestDetail(@RequestBody UpdateActualStockCheck request) {
        LOGGER.info("Resetting tracking for stock check request detail with data: {}", request);
        stockCheckDetailService.resetTrackingForStockCheckRequestDetail(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully reset tracking for stock check request detail"
        );
    }
}
