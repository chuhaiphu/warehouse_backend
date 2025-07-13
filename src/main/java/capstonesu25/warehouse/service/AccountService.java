package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.enums.TokenType;
import capstonesu25.warehouse.model.account.*;
import capstonesu25.warehouse.repository.*;
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

import java.time.Duration;
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
    private final ImportOrderRepository importOrderRepository;
    private final ExportRequestRepository exportRequestRepository;
    private final ItemRepository itemRepository;
    private final ConfigurationRepository configurationRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        Account account = accountRepository.findByUsername(request.getUsername())
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
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        Account account = Account.builder()
                .username(request.getUsername())
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
                .username(savedAccount.getUsername())
                .email(savedAccount.getEmail())
                .phone(savedAccount.getPhone())
                .fullName(savedAccount.getFullName())
                .role(savedAccount.getRole())
                .status(savedAccount.getStatus())
                .isEnable(savedAccount.getIsEnable())
                .isBlocked(savedAccount.getIsBlocked())
                .build();
    }

    public List<AccountResponse> checkAnyKeepersIsAvailableInDate(CheckAnyKeepersIsAvailableInDateRequest request) {
        LOGGER.info("Checking available staff on date: {}", request.getDate());
        LocalDate date = request.getDate();
        int totalMinutes = 0;

        // Tính tổng thời gian cần thiết để thực hiện tất cả task
        for (CheckAnyKeepersIsAvailableInDateRequest.ListItems itemRq : request.getListItems()) {
            Item item = itemRepository.findById(itemRq.getItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

            int taskCount;
            if (itemRq.getQuantity() == null || itemRq.getQuantity() == 0) {
                if (item.getMeasurementValue() == null || item.getMeasurementValue() == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid item measurement value");
                }
                taskCount = (int) Math.ceil(itemRq.getMeasurementValue() / item.getMeasurementValue());
            } else {
                taskCount = itemRq.getQuantity();
            }

            totalMinutes += taskCount * item.getCountingMinutes();
        }

        LOGGER.info("Total required working time for task: {} minutes", totalMinutes);

        // Lấy danh sách nhân viên có vai trò STAFF và đang ACTIVE
        List<Account> accounts = accountRepository.findByRoleAndStatus(AccountRole.STAFF, AccountStatus.ACTIVE);
        Configuration config = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Configuration not found"));

        long expectedWorkingMinutesPerDay = Duration.between(config.getWorkingTimeStart(), config.getWorkingTimeEnd()).toMinutes();
        LOGGER.info("Expected working time per day: {} minutes", expectedWorkingMinutesPerDay);
        List<AccountResponse> availableStaffs = new ArrayList<>();

        for (Account account : accounts) {
            List<StaffPerformance> performancesOnDate = account.getStaffPerformances().stream()
                    .filter(p -> p.getDate().equals(date))
                    .toList();

            if (performancesOnDate.isEmpty()) {
                LOGGER.info("Account {} has no performance record on {}", account.getEmail(), date);
                availableStaffs.add(new AccountResponse(
                        account.getId(),
                        account.getEmail(),
                        account.getPhone(),
                        account.getFullName(),
                        account.getStatus(),
                        account.getIsEnable(),
                        account.getIsBlocked(),
                        account.getRole(),
                        LocalTime.of(0, 0),
                        LocalTime.ofSecondOfDay(expectedWorkingMinutesPerDay * 60),
                        account.getImportOrders() != null ?
                                account.getImportOrders().stream().map(ImportOrder::getId).toList() :
                                List.of(),
                        account.getExportRequests() != null ?
                                account.getExportRequests().stream().map(ExportRequest::getId).toList() :
                                List.of()
                ));
                continue;
            }

            // Total actual working time trong ngày
            long actualMinutes = performancesOnDate.stream()
                    .filter(p -> p.getExpectedWorkingTime() != null)
                    .mapToLong(p -> p.getExpectedWorkingTime().toSecondOfDay() / 60)
                    .sum();

            long freeTime = expectedWorkingMinutesPerDay - actualMinutes;

            if (freeTime >= totalMinutes) {
                LOGGER.info("Account {} has enough free time: {} minutes", account.getEmail(), freeTime);
                availableStaffs.add(new AccountResponse(
                        account.getId(),
                        account.getEmail(),
                        account.getPhone(),
                        account.getFullName(),
                        account.getStatus(),
                        account.getIsEnable(),
                        account.getIsBlocked(),
                        account.getRole(),
                        LocalTime.ofSecondOfDay(actualMinutes * 60),
                        LocalTime.ofSecondOfDay(expectedWorkingMinutesPerDay * 60),
                        account.getImportOrders() != null ?
                                account.getImportOrders().stream().map(ImportOrder::getId).toList() :
                                List.of(),
                        account.getExportRequests() != null ?
                                account.getExportRequests().stream().map(ExportRequest::getId).toList() :
                                List.of()
                ));
            }
        }


        LOGGER.info("Total available staff: {}", availableStaffs.size());
        return availableStaffs;
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

    public AccountResponse findAccountByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
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

    public List<AccountResponse> getAllActiveStaffsInDate(ActiveAccountRequest request) {
        LOGGER.info("Get all active staffs ");
        LocalDate date = request.getDate();
        List<Account> accounts = accountRepository.findByRoleAndStatus(
                AccountRole.STAFF,
                AccountStatus.ACTIVE
        );

        if((request.getExportRequestId() != null && request.getImportOrderId() != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");

        }

        List <AccountResponse> accountResponses = new ArrayList<>();
        List<AccountResponse> responses = new ArrayList<>();
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
        LOGGER.info("Total expected working time of request in day: {}", accountResponses.size());
        accountResponses.sort(Comparator.comparing(AccountResponse::getTotalExpectedWorkingTimeOfRequestInDay));
        if(request.getImportOrderId() == null && request.getExportRequestId() == null) {
            LOGGER.info("Get all active staffs in date {} with no import order Id and export request Id", date);
            return accountResponses;
        }
        if(request.getImportOrderId() != null) {
            LOGGER.info("Get all active staffs in date {} for import order", date);
            ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Import order not found"));
            LOGGER.info("Import order date: {} and Request date: {}", importOrder.getDateReceived(), request.getDate());
            if(!importOrder.getDateReceived().equals(request.getDate())) {
                LOGGER.info("Import order date {} does not match request date {}", importOrder.getDateReceived(), request.getDate());
                return accountResponses;
            }

            for(AccountResponse accountResponse : accountResponses) {
               List<ImportOrder> checkImportOrder = importOrderRepository.findByAssignedStaff_IdAndDateReceived(
                       accountResponse.getId(),
                       importOrder.getDateReceived()
               );
               if(checkImportOrder.isEmpty()) {
                   LOGGER.info("Account {} has no import order on date {}", accountResponse.getEmail(), date);
                   responses.add(accountResponse);
               }
               else {
                   for (ImportOrder orderCheck : checkImportOrder) {
                       int totalMinutes = 0;
                       for (ImportOrderDetail detail : orderCheck.getImportOrderDetails()) {
                           LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
                           totalMinutes += detail.getExpectQuantity() * detail.getItem().getCountingMinutes();
                       }
                       LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
                       if (importOrder.getTimeReceived().isAfter(orderCheck.getTimeReceived().plusMinutes(expectedWorkingTime.toSecondOfDay() / 60))) {
                           responses.add(accountResponse);
                       }
                   }
               }
            }
        }

        if(request.getExportRequestId() != null) {
            LOGGER.info("Get all active staffs in date {} for export request", date);
            ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Export request not found"));

            if(!exportRequest.getExportDate().equals(request.getDate())) {
                return accountResponses;
            }

            for(AccountResponse accountResponse : accountResponses) {
                List<ExportRequest> checkExportRequest = exportRequestRepository.findAllByCountingStaffIdAndCountingDate(
                        accountResponse.getId(),
                        exportRequest.getCountingDate()
                );
                if(checkExportRequest.isEmpty()) {
                    responses.add(accountResponse);
                }
                for(ExportRequest exportCheck : checkExportRequest) {
                    int totalMinutes = 0;
                    for (ExportRequestDetail detail : exportCheck.getExportRequestDetails()) {
                        LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
                        totalMinutes += detail.getQuantity() * detail.getItem().getCountingMinutes();
                    }
                    LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
                    if(exportCheck.getCountingTime().isAfter(exportCheck.getCountingTime().plusMinutes(expectedWorkingTime.toSecondOfDay() / 60))) {
                        responses.add(accountResponse);
                    }
                }
            }
        }

        return responses;
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