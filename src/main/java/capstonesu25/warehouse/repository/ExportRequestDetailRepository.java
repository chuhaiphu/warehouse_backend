package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ExportRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportRequestDetailRepository extends JpaRepository<ExportRequestDetail, Long> {
    List<ExportRequestDetail> findByExportRequestId(Long exportRequestId);

    // Find export request details by item id
    List<ExportRequestDetail> findByItemId(Long itemId);

    // Find export request details by status
    List<ExportRequestDetail> findByStatus(String status);

    // Check if exists by id
    boolean existsById(Long id);
}
