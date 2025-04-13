package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ExportRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportRequestRepository extends JpaRepository<ExportRequest, Long> {
    Page<ExportRequest> findAllByAssignedStaff_Id(Long staffId, Pageable pageable);
}
