package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ExportRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ExportRequestDetailRepository extends JpaRepository<ExportRequestDetail, Long> {
}
