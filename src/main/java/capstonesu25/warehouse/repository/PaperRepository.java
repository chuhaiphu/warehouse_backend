package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long>{
    Page<Paper> findPapersByImportOrder_Id(String importOrderId, Pageable pageable);
    Page<Paper> findPapersByExportRequest_Id(String exportRequestId, Pageable pageable);
}
