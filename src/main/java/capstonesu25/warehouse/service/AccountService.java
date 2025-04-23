package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.StaffPerformance;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.enums.TokenType;
import capstonesu25.warehouse.model.account.*;
import capstonesu25.warehouse.repository.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements LogoutHandler {
    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account);
        
        account.setRefreshToken(refreshToken);
        accountRepository.save(account);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public RegisterResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        Account account = Account.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AccountRole.valueOf(request.getRole()))
                .status(AccountStatus.ACTIVE)
                .isEnable(true)
                .isBlocked(false)
                .build();

        Account savedAccount = accountRepository.save(account);

        return RegisterResponse.builder()
                .id(savedAccount.getId())
                .email(savedAccount.getEmail())
                .phone(savedAccount.getPhone())
                .fullName(savedAccount.getFullName())
                .role(savedAccount.getRole())
                .status(savedAccount.getStatus())
                .isEnable(savedAccount.getIsEnable())
                .isBlocked(savedAccount.getIsBlocked())
                .build();
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is missing");
        }

        final String refreshToken = authHeader.substring(7);
        final String accountId = jwtService.extractAccountId(refreshToken, TokenType.REFRESH);
        if (accountId != null) {
            Account account = accountRepository.findById(Long.parseLong(accountId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

            if (jwtService.isTokenValid(refreshToken, account, TokenType.REFRESH)) {
                String accessToken = jwtService.generateAccessToken(account);
                
                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    public List<AccountResponse> getAccountsByRole(AccountRole role) {
        return accountRepository.findByRole(role).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<AccountResponse> getAccountsByRoleWithPagination(AccountRole role, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Account> accounts = accountRepository.findByRole(role, pageable);
        return accounts.map(this::mapToResponse);
    }

    public AccountResponse findAccountByEmail(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return mapToResponse(account);
    }

    public AccountResponse findAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return mapToResponse(account);
    }

    public List<AccountResponse> getActiveStaffs() {
        return accountRepository.findByRoleAndStatus(AccountRole.STAFF, AccountStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AccountResponse> getAllActiveStaffsInDate(LocalDate date) {
        LOGGER.info("Get all active staffs ");
        List<Account> accounts = accountRepository.findByRoleAndStatus(
                AccountRole.STAFF,
                AccountStatus.ACTIVE
        );

        List <AccountResponse> accountResponses = new ArrayList<>();
        for(Account account : accounts) {
            List<StaffPerformance> staffPerformances = account.getStaffPerformances()
                    .stream()
                    .filter(staffPerformance -> staffPerformance.getDate().equals(date))
                    .toList();
            if (staffPerformances.isEmpty()) {
                LOGGER.warn("Account {} has no performance on date {}", account.getEmail(), date);
            }
            LocalTime totalActualWorkingTimeOfRequestInDay = LocalTime.of(0, 0);
            LocalTime totalExpectedWorkingTimeOfRequestInDay = LocalTime.of(0, 0);

            for(StaffPerformance performance : staffPerformances) {
                totalExpectedWorkingTimeOfRequestInDay = totalExpectedWorkingTimeOfRequestInDay.
                        plusMinutes(performance.getExpectedWorkingTime().toSecondOfDay() / 60);

                if (performance.getActualWorkingTime() != null) {
                    totalActualWorkingTimeOfRequestInDay = totalActualWorkingTimeOfRequestInDay.
                            plusMinutes(performance.getActualWorkingTime().toSecondOfDay() / 60);
                }
            }
            AccountResponse accountResponse = new AccountResponse(
                    account.getId(),
                    account.getEmail(),
                    account.getPhone(),
                    account.getFullName(),
                    account.getStatus(),
                    account.getIsEnable(),
                    account.getIsBlocked(),
                    account.getRole(),
                    totalActualWorkingTimeOfRequestInDay,
                    totalExpectedWorkingTimeOfRequestInDay,
                    account.getImportOrders() != null ?
                            account.getImportOrders().stream().map(ImportOrder::getId).toList() :
                            List.of(),
                    account.getExportRequests() != null ?
                            account.getExportRequests().stream().map(ExportRequest::getId).toList() :
                            List.of()
            );
            accountResponses.add(accountResponse);
        }
        accountResponses.forEach(accountResponse -> {
            if (accountResponse.getTotalExpectedWorkingTimeOfRequestInDay() == null) {
                accountResponse.setTotalActualWorkingTimeOfRequestInDay(LocalTime.of(0, 0));
            }
        });
        accountResponses.sort(Comparator.comparing(AccountResponse::getTotalExpectedWorkingTimeOfRequestInDay));

        return accountResponses;
    }



    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String jwt = authHeader.substring(7);
        Account account = accountRepository.findByRefreshToken(jwt).orElse(null);
        if (account != null) {
            account.setRefreshToken(null);
            accountRepository.save(account);
        }
        SecurityContextHolder.clearContext();
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getPhone(),
                account.getFullName(),
                account.getStatus(),
                account.getIsEnable(),
                account.getIsBlocked(),
                account.getRole(),
                null,
                null,
                account.getImportOrders() != null ?
                        account.getImportOrders().stream().map(ImportOrder::getId).toList() :
                        List.of(),
                account.getExportRequests() != null ?
                        account.getExportRequests().stream().map(ExportRequest::getId).toList() :
                        List.of()
        );
    }
}