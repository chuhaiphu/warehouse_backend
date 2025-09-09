package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.StockCheckRequest;
import capstonesu25.warehouse.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExportRequestRepository extends JpaRepository<ExportRequest, String> {
    Page<ExportRequest> findAllByAssignedStaff_Id(Long staffId, Pageable pageable);

    Page<ExportRequest> findAllByCountingStaffId(Long staffId, Pageable pageable);

    List<ExportRequest> findAllByCountingStaffIdAndCountingDate(
            Long staffId, LocalDate date);

    List<ExportRequest> findAllByAssignedStaff_IdAndExportDate(
            Long staffId, LocalDate date);

    List<ExportRequest> findByExportDateAndStatusIn(
            LocalDate exportDate, List<RequestStatus> statuses);

    List<ExportRequest> findByStatusAndExtendedDateLessThanEqual(
            RequestStatus status, LocalDate extendedDate);

    List<ExportRequest> findByCountingDateAndCountingStaffId(
            LocalDate countingDate, Long countingStaffId);

    List<ExportRequest> findByExportDateAndExportDate(
            LocalDate exportDate, LocalDate exportDate2);

    List<ExportRequest> findAllByStatus(RequestStatus status);

    @Query("SELECT COUNT(er) FROM ExportRequest er WHERE er.createdDate BETWEEN :start AND :end")
    int countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM ExportRequest s " +
            "WHERE s.createdDate BETWEEN :start AND :end")
    List<ExportRequest> findByCreatedDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<ExportRequest> findByIdStartingWith(String idPrefix);

    List<ExportRequest> findAllByStatusIn(List<RequestStatus> statuses);

}
