package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByRole(AccountRole role);
    Page<Account> findByRole(AccountRole role, Pageable pageable);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Optional<Account> findByRefreshToken(String jwt);
    Optional<Account> findByVerificationToken(String token);
    List<Account> findByRoleAndStatus(AccountRole accountRole, AccountStatus accountStatus);
    @Query("SELECT DISTINCT a FROM Account a " +
            "JOIN a.staffPerformances sp " +
            "WHERE a.role = :role AND a.status = :status AND sp.date = :date")
    List<Account> findActiveStaffsWithPerformanceOnDate(
            @Param("role") AccountRole role,
            @Param("status") AccountStatus status,
            @Param("date") LocalDate date);
}
