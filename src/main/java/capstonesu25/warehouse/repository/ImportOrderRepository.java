package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.enums.ImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImportOrderRepository extends JpaRepository<ImportOrder, String>{
    Page<ImportOrder> findImportOrdersByImportRequest_Id(String id, Pageable pageable);
    Page<ImportOrder> findImportOrdersByAssignedStaff_Id(Long staffId, Pageable pageable);
    List<ImportOrder> findByAssignedStaff_IdAndDateReceived(Long staffId, LocalDate dateReceived);
    List<ImportOrder> findByDateReceivedAndStatus(LocalDate dateReceived, ImportStatus status);

    List<ImportOrder> findByStatusAndExtendedDateLessThanEqual(ImportStatus status, LocalDate date);

}
