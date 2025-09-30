package capstonesu25.warehouse.service;

import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.model.account.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.*;

public interface AccountService {
     AuthenticationResponse authenticate(AuthenticationRequest request);

     RegisterResponse register(RegisterRequest request);

     AccountResponse updateAccount(UpdateAccountRequest request);

     List<AccountResponse> checkAnyKeepersIsAvailableInDate(CheckAnyKeepersIsAvailableInDateRequest request);

     AuthenticationResponse refreshToken(HttpServletRequest request);

     List<AccountResponse> getAccountsByRole(AccountRole role);

     Page<AccountResponse> getAccountsByRoleWithPagination(AccountRole role, int page, int limit);

     AccountResponse findAccountByEmail(String email);

     AccountResponse findAccountByUsername(String username);

     AccountResponse findAccountById(Long id);

     List<AccountResponse> getActiveStaffs();
     List<AccountResponse> getAccountsByDepartmentId (Long departmentId);
     TaskOfStaffPerDate getTasksOfStaffPerDate(Long staffId, LocalDate date);

     List<AccountResponse> getAllActiveStaffsInDate(ActiveAccountRequest request);

}
