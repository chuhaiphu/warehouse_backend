package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ImportRequestDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportRequestDetailRepository extends JpaRepository<ImportRequestDetail, Long>{
    Page<ImportRequestDetail> findImportRequestDetailsByImportRequest_Id(String importRequestId, Pageable pageable);
    List<ImportRequestDetail> findImportRequestDetailsByImportRequest_Id(String importRequestId);
}
