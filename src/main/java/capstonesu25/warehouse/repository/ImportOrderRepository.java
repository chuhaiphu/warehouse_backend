package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ImportOrderRepository extends JpaRepository<ImportOrder, String>{
    List<ImportOrder> findImportOrdersByImportRequest_Id(String id);
    Page<ImportOrder> findImportOrdersByAssignedStaff_Id(Long staffId, Pageable pageable);
    List<ImportOrder> findByAssignedStaff_IdAndDateReceived(Long staffId, LocalDate dateReceived);
    List<ImportOrder> findByDateReceivedAndStatus(LocalDate dateReceived, RequestStatus status);
    List<ImportOrder> findByStatusIn(List<RequestStatus> statuses);
    List<ImportOrder> findByStatusAndExtendedDateLessThanEqual(RequestStatus status, LocalDate date);

    List<ImportOrder> findAllByStatus(RequestStatus status);


}
