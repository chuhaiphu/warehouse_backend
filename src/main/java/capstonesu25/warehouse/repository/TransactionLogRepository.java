package capstonesu25.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import capstonesu25.warehouse.entity.TransactionLog;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

}
