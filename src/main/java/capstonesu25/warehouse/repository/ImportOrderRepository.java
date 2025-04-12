package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ImportOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportOrderRepository extends JpaRepository<ImportOrder, Long>{
    Page<ImportOrder> findImportOrdersByImportRequest_Id(Long id, Pageable pageable);
    Page<ImportOrder> findImportOrdersByAssignedStaff_Id(Long staffId, Pageable pageable);
}
