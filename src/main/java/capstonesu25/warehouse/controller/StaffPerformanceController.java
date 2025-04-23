package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.service.StaffPerformanceService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/staff-performance")
@RequiredArgsConstructor
@Validated
public class StaffPerformanceController {
    private final StaffPerformanceService staffPerformanceService;
    private static final Logger LOGGER = LoggerFactory.getLogger(StaffPerformanceController.class);

    @Operation(summary = "Get all staff performance records for a specific date")
    @GetMapping("/{date}")
    public ResponseEntity<?> getAllStaffPerformance(@PathVariable LocalDate date) {
        LOGGER.info("Fetching all staff performance records for date: {}", date);
        return ResponseUtil.getCollection(
                staffPerformanceService.getAllActive(date),
                HttpStatus.OK,
                "Staff performance records retrieved successfully",
                null
        );
    }

    @Operation(summary = "Get staff performance records by account ID and date")
    @GetMapping("/{accountId}/{date}")
    public ResponseEntity<?> getStaffPerformanceByAccountIdAndDate(@PathVariable Long accountId, @PathVariable LocalDate date) {
        LOGGER.info("Fetching staff performance records for account ID: {} and date: {}", accountId, date);
        return ResponseUtil.getCollection(
                staffPerformanceService.getAllByAccountIdAndDate(accountId, date),
                HttpStatus.OK,
                "Staff performance records retrieved successfully",
                null
        );
    }

}
