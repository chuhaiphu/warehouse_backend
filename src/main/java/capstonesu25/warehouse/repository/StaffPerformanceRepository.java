package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.StaffPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StaffPerformanceRepository extends JpaRepository<StaffPerformance, Long> {
    List<StaffPerformance> findByAssignedStaff_IdAndDate(Long accountId, LocalDate date);
    StaffPerformance findByImportOrderIdAndAssignedStaff_Id(String importOrderId, Long accountId);
    StaffPerformance findByExportRequestIdAndAssignedStaff_IdAndExportCounting(String exportRequestId, Long accountId, boolean isExportCounting);
}
