package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.service.AccountService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @Operation(summary = "Get all accounts by role")
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getAccountsByRole(@PathVariable AccountRole role) {
        LOGGER.info("Getting all accounts by role: {}", role);
        return ResponseUtil.getCollection(
                accountService.getAccountsByRole(role),
                HttpStatus.OK,
                "Successfully retrieved accounts by role",
                null
        );
    }
} 