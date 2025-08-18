package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.StockCheckRequest;
import capstonesu25.warehouse.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImportRequestRepository extends JpaRepository<ImportRequest, String> {
    List<ImportRequest> findByBatchCodeStartingWith(String batchCodePrefix);

    @Query("SELECT COUNT(ir) FROM ImportRequest ir WHERE ir.createdDate BETWEEN :start AND :end")
    int countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM ImportRequest s " +
            "WHERE s.createdDate BETWEEN :start AND :end")
    List<ImportRequest> findByCreatedDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<ImportRequest> findByIdStartingWith(String idPrefix);

    List<ImportRequest> findAllByStatus(RequestStatus status);
}
