package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.model.account.*;
import capstonesu25.warehouse.service.AccountService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Validated
public class AccountController {
    private final AccountService accountService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @Operation(summary = "Register new account")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        LOGGER.info("Registering new account with email: {}", request.getEmail());
        RegisterResponse response = accountService.register(request);
        return ResponseUtil.getObject(
                response,
                HttpStatus.CREATED,
                "Account registered successfully"
        );
    }

    @Operation(summary = "Authenticate user")
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        LOGGER.info("Authenticating user with email: {}", request.getEmail());
        AuthenticationResponse response = accountService.authenticate(request);
        return ResponseUtil.getObject(
                response,
                HttpStatus.OK,
                "Authentication successful"
        );
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        LOGGER.info("Refreshing access token");
        AuthenticationResponse response = accountService.refreshToken(request);
        return ResponseUtil.getObject(
                response,
                HttpStatus.OK,
                "Token refreshed successfully"
        );
    }

    @Operation(summary = "Get all accounts by role")
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getAccountsByRole(@PathVariable AccountRole role) {
        LOGGER.info("Getting all accounts by role: {}", role);
        List<AccountResponse> accounts = accountService.getAccountsByRole(role);
        return ResponseUtil.getCollection(
                accounts,
                HttpStatus.OK,
                "Successfully retrieved accounts by role",
                null
        );
    }

    @Operation(summary = "Get accounts by role with pagination")
    @GetMapping("/role/{role}/paged")
    public ResponseEntity<?> getAccountsByRoleWithPagination(
            @PathVariable AccountRole role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting paginated accounts by role: {}, page: {}, limit: {}", role, page, limit);
        Page<AccountResponse> accounts = accountService.getAccountsByRoleWithPagination(role, page, limit);
        return ResponseUtil.getCollection(
                accounts,
                HttpStatus.OK,
                "Successfully retrieved paginated accounts by role",
                null
        );
    }

    @Operation(summary = "Find account by email")
    @GetMapping("/by-email")
    public ResponseEntity<?> findAccountByEmail(@RequestParam String email) {
        LOGGER.info("Finding account by email: {}", email);
        AccountResponse account = accountService.findAccountByEmail(email);
        return ResponseUtil.getObject(
                account,
                HttpStatus.OK,
                "Account found successfully"
        );
    }

    @Operation(summary = "Test authentication endpoint")
    @GetMapping("/test-authentication")
    public ResponseEntity<?> testAuthentication() {
        LOGGER.info("Testing authentication");
        return ResponseUtil.getObject(
                "Authentication successful",
                HttpStatus.OK,
                "Authentication test passed"
        );
    }

    @Operation(summary = "Test authorization endpoint")
    @GetMapping("/test-authorization")
    public ResponseEntity<?> testAuthorization() {
        LOGGER.info("Testing authorization");
        return ResponseUtil.getObject(
                "Authorization successful",
                HttpStatus.OK,
                "Authorization test passed"
        );
    }
}