package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ExportRequestDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ExportRequestDetailRepository extends JpaRepository<ExportRequestDetail, Long> {
    Page<ExportRequestDetail> findExportRequestDetailByExportRequest_Id(String exportRequestId, Pageable pageable);
}
