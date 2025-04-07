package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

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
                account.getImportOrders() != null ?
                        account.getImportOrders().stream().map(ImportOrder::getId).toList() :
                        List.of(),
                account.getExportRequests() != null ?
                        account.getExportRequests().stream().map(ExportRequest::getId).toList() :
                        List.of()
        );
    }
} 