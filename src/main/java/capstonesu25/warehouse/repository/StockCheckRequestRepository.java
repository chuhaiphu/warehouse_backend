package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.StockCheckRequest;
import capstonesu25.warehouse.enums.RequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockCheckRequestRepository extends JpaRepository<StockCheckRequest, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(er) FROM ExportRequest er WHERE er.createdDate BETWEEN :start AND :end")
    int countByCreatedAtBetweenLocked(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM StockCheckRequest s " +
            "WHERE s.createdDate BETWEEN :start AND :end")
    List<StockCheckRequest> findByCreatedDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    List<StockCheckRequest> findByAssignedStaff_IdAndCountingDate(
            Long staffId, LocalDate countingDate);

    List<StockCheckRequest> findByAssignedStaff_Id(Long staffId);

    List<StockCheckRequest> findByIdStartingWith(String idPrefix);

    List<StockCheckRequest> findAllByStatus(RequestStatus status);
}
