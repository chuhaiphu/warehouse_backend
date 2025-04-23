package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByRole(AccountRole role);
    Page<Account> findByRole(AccountRole role, Pageable pageable);
    Optional<Account> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<Account> findByRefreshToken(String jwt);
    Optional<Account> findByVerificationToken(String token);
    List<Account> findByRoleAndStatus(AccountRole accountRole, AccountStatus accountStatus);
    List<Account> findDistinctByRoleAndStatusAndStaffPerformances_Date(AccountRole accountRole, AccountStatus accountStatus, LocalDate date);
}
