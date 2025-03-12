package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ExportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportRequestRepository extends JpaRepository<ExportRequest, Long> {
}
