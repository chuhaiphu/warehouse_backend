package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.enums.AccountRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByRole(AccountRole role);
    Page<Account> findByRole(AccountRole role, Pageable pageable);
}
