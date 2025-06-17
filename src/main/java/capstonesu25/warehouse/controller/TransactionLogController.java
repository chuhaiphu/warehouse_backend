package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.service.TransactionLogService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transaction-log")
@RequiredArgsConstructor
public class TransactionLogController {
    private final TransactionLogService transactionLogService;

    @Operation(summary = "Get all transaction logs")
    @GetMapping()
    public ResponseEntity<?> getAllTransactionLogs(){
        return ResponseUtil.getCollection(
                transactionLogService.getAllTransactionLogs(),
                HttpStatus.OK,
                "Fetch all transaction logs successfully",
                null
        );
    }
} 